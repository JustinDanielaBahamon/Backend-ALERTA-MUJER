package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.RecuperarContrasenaUseCase;
import com.alertamujer.identity.application.port.out.CifradoPort;
import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.application.port.out.EventosIdentidadPort;
import com.alertamujer.identity.application.port.out.RecuperacionRepositorio;
import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import com.alertamujer.identity.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Recuperación de contraseña con un código de 6 dígitos enviado al correo (web y app móvil). */
@Service
public class RecuperacionContrasenaService implements RecuperarContrasenaUseCase {

    static final String CODIGO_INVALIDO = "El código no es válido o ya venció. Pide uno nuevo.";

    private final UsuariaRepositorio usuarias;
    private final CuentaRepositorio cuentas;
    private final RecuperacionRepositorio solicitudes;
    private final CifradoPort cifrado;
    private final EventosIdentidadPort events;
    private final SecureRandom random = new SecureRandom();

    public RecuperacionContrasenaService(UsuariaRepositorio usuarias, CuentaRepositorio cuentas,
                                         RecuperacionRepositorio solicitudes, CifradoPort cifrado,
                                         EventosIdentidadPort events) {
        this.usuarias = usuarias;
        this.cuentas = cuentas;
        this.solicitudes = solicitudes;
        this.cifrado = cifrado;
        this.events = events;
    }

    @Override
    @Transactional
    public void solicitar(String email, String ip) {
        Optional<User> usuaria = usuarias.porCorreo(normalizar(email));
        Optional<Account> cuenta = usuaria.flatMap(u -> cuentas.deUsuaria(u.getId()));
        if (cuenta.isEmpty() || cuenta.get().bloqueada()) return;
        Long accountId = cuenta.get().getId();
        LocalDateTime ahora = LocalDateTime.now();

        List<RecoveryRequest> pendientes = solicitudes.pendientesDeCuenta(accountId);
        if (pendientes.stream().anyMatch(r -> r.reciente(ahora))) return;
        pendientes.forEach(RecoveryRequest::reemplazar);
        solicitudes.guardarTodas(pendientes);

        String codigo;
        do {
            codigo = String.format("%06d", random.nextInt(1_000_000));
        } while (solicitudes.existeHash(RecoveryRequest.hash(accountId, codigo)));
        solicitudes.save(RecoveryRequest.nueva(accountId, codigo, ip, ahora));

        User u = usuaria.get();
        events.codigoRecuperacion(u.getEmail(), u.nombreCompleto(), codigo, RecoveryRequest.MINUTOS_VALIDEZ);
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verificar(String email, String codigo) {
        validate(email, codigo);
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void restablecer(String email, String codigo, String nuevaContrasena) {
        Account.validarContrasenaNueva(nuevaContrasena);
        RecoveryRequest solicitud = validate(email, codigo);
        Account cuenta = cuentas.find(solicitud.getAccountId()).orElseThrow(() -> new IllegalArgumentException(CODIGO_INVALIDO));
        cuenta.cambiarContrasena(cifrado.cifrar(nuevaContrasena));
        cuentas.save(cuenta);
        solicitud.usar(LocalDateTime.now());
        solicitudes.save(solicitud);
        if (usuarias.find(cuenta.getUserId()).map(User::esAdmin).orElse(false)) {
            events.sesionAdmin(cuenta.getId(), AdminSessionEvent.PASSWORD_CHANGED, "Recuperación por correo");
        }
    }

    private RecoveryRequest validate(String email, String codigo) {
        if (codigo == null || !codigo.matches("\\d{6}")) throw new IllegalArgumentException(CODIGO_INVALIDO);
        Account cuenta = usuarias.porCorreo(normalizar(email))
                .flatMap(u -> cuentas.deUsuaria(u.getId()))
                .filter(a -> !a.bloqueada())
                .orElseThrow(() -> new IllegalArgumentException(CODIGO_INVALIDO));
        RecoveryRequest solicitud = solicitudes.pendientesDeCuenta(cuenta.getId()).stream()
                .max(Comparator.comparing(RecoveryRequest::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new IllegalArgumentException(CODIGO_INVALIDO));
        if (solicitud.vencida(LocalDateTime.now())) {
            solicitud.expirar();
            solicitudes.save(solicitud);
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }
        if (!solicitud.coincide(codigo)) {
            solicitud.registrarFallo();
            solicitudes.save(solicitud);
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }
        return solicitud;
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim();
    }
}
