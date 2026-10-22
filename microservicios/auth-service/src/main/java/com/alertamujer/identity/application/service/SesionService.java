package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.DatosRegistro;
import com.alertamujer.identity.application.port.in.IniciarSesionUseCase;
import com.alertamujer.identity.application.port.in.RegistrarUsuariaUseCase;
import com.alertamujer.identity.application.port.in.Sesion;
import com.alertamujer.identity.application.port.out.CifradoPort;
import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.application.port.out.EventosIdentidadPort;
import com.alertamujer.identity.application.port.out.PerfilRepositorio;
import com.alertamujer.identity.application.port.out.TokensPort;
import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.application.port.out.VerificadorGooglePort;
import com.alertamujer.identity.application.port.out.VerificadorGooglePort.IdentidadGoogle;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Registro, inicio de sesión (correo o Google), renovación y cierre. Solo conoce puertos. */
@Service
public class SesionService implements RegistrarUsuariaUseCase, IniciarSesionUseCase {

    private final UsuariaRepositorio usuarias;
    private final CuentaRepositorio cuentas;
    private final PerfilRepositorio perfiles;
    private final CifradoPort cifrado;
    private final TokensPort tokens;
    private final VerificadorGooglePort google;
    private final EventosIdentidadPort events;

    public SesionService(UsuariaRepositorio usuarias, CuentaRepositorio cuentas, PerfilRepositorio perfiles,
                         CifradoPort cifrado, TokensPort tokens, VerificadorGooglePort google,
                         EventosIdentidadPort events) {
        this.usuarias = usuarias;
        this.cuentas = cuentas;
        this.perfiles = perfiles;
        this.cifrado = cifrado;
        this.tokens = tokens;
        this.google = google;
        this.events = events;
    }

    // ── Registro ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Sesion registrar(DatosRegistro d) {
        if (Boolean.FALSE.equals(d.aceptaTerminos())) {
            throw new IllegalArgumentException("Debes aceptar los términos y la política de privacidad");
        }
        String telefono = User.limpiarTelefono(d.telefono());
        String documento = User.vacioANull(d.numeroDocumento());
        Map<String, String> repetidos = new LinkedHashMap<>();
        if (usuarias.existeCorreo(d.email())) repetidos.put("email", "Este correo ya está registrado");
        if (telefono != null && usuarias.existeTelefono(telefono)) {
            repetidos.put("telefono", "Este número de teléfono ya está registrado");
        }
        if (documento != null && usuarias.existeDocumento(documento)) {
            repetidos.put("numeroDocumento", "Este número de documento ya está registrado");
        }
        if (!repetidos.isEmpty()) throw new DuplicateFieldsException(repetidos);

        usuarias.asegurarRolUsuaria();
        User nueva = User.nueva(d.name(), d.email(), d.telefono(), d.tipoDocumento(),
                d.numeroDocumento(), User.fechaDeFormulario(d.fechaNacimiento()));
        // Solo queda constancia si la casilla llegó marcada (las apps anteriores no la envían)
        if (Boolean.TRUE.equals(d.aceptaTerminos())) nueva.aceptarTerminos(LocalDateTime.now());
        User usuaria = usuarias.save(nueva);
        cuentas.save(Account.nueva(usuaria.getId(), cifrado.cifrar(d.password())));
        // Sin perfil no puede crear alertas, contactos ni ubicaciones
        if (!perfiles.existeDeUsuaria(usuaria.getId())) perfiles.save(UserProfile.of(usuaria.getId()));
        return abrir(usuaria);
    }

    // ── Inicio de sesión ────────────────────────────────────────────

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public Sesion conCorreo(String email, String contrasena) {
        User usuaria = usuarias.porCorreo(email).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        Account cuenta = cuentaDe(usuaria);
        if (cuenta.bloqueada()) {
            auditar(usuaria, cuenta, AdminSessionEvent.LOGIN_FAILED, "Cuenta bloqueada");
            throw new InvalidCredentialsException("Account blocked");
        }
        if (!cifrado.coincide(contrasena, cuenta.getPasswordHash())) {
            auditar(usuaria, cuenta, AdminSessionEvent.LOGIN_FAILED, "Contraseña incorrecta");
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return entrar(usuaria, cuenta, "Con correo y contraseña");
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public Sesion conGoogle(String idToken) {
        IdentidadGoogle id = google.verificar(idToken);
        User usuaria = usuarias.porCorreo(id.email()).orElseGet(() -> crearDesdeGoogle(id));
        Account cuenta = cuentaDe(usuaria);
        if (cuenta.bloqueada()) {
            auditar(usuaria, cuenta, AdminSessionEvent.LOGIN_FAILED, "Cuenta bloqueada (Google)");
            throw new InvalidCredentialsException("Account blocked");
        }
        return entrar(usuaria, cuenta, "Con Google");
    }

    @Override
    public String renovar(String refreshToken) {
        try {
            User usuaria = usuarias.find(tokens.usuariaDeRenovacion(refreshToken)).orElseThrow();
            if (!cuentaDe(usuaria).puedeRenovarSesion()) throw new InvalidCredentialsException("Account blocked");
            return tokens.acceso(usuaria.getId(), usuaria.rolDelToken());
        } catch (RuntimeException e) {
            // Mismo mensaje para cualquier motivo: no se da pistas sobre el token
            throw new InvalidCredentialsException("Refresh token inválido o expirado");
        }
    }

    @Override
    public void cerrar(Long userId, String motivo) {
        if (userId == null) return;
        usuarias.find(userId).filter(User::esAdmin).ifPresent(u -> cuentas.deUsuaria(userId).ifPresent(c ->
                events.sesionAdmin(c.getId(), AdminSessionEvent.LOGOUT, "inactividad".equals(motivo) ? "Por inactividad" : "Voluntario")));
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Sesion entrar(User usuaria, Account cuenta, String como) {
        cuenta.registrarAcceso(LocalDateTime.now());
        cuentas.save(cuenta);
        auditar(usuaria, cuenta, AdminSessionEvent.LOGIN_SUCCESS, como);
        return abrir(usuaria);
    }

    private Sesion abrir(User usuaria) {
        return new Sesion(tokens.acceso(usuaria.getId(), usuaria.rolDelToken()), tokens.renovacion(usuaria.getId()), usuaria);
    }

    private User crearDesdeGoogle(IdentidadGoogle id) {
        usuarias.asegurarRolUsuaria();
        User nueva = User.desdeGoogle(id.email(), id.name(), id.givenName(), id.familyName());
        // La pantalla de inicio avisa que continuar con Google es aceptar los términos
        nueva.aceptarTerminos(LocalDateTime.now());
        User usuaria = usuarias.save(nueva);
        // Contraseña aleatoria imposible de adivinar: la cuenta se usa con Google
        cuentas.save(Account.nueva(usuaria.getId(), cifrado.cifrar(UUID.randomUUID() + UUID.randomUUID().toString())));
        perfiles.save(UserProfile.of(usuaria.getId()));
        return usuaria;
    }

    private Account cuentaDe(User usuaria) {
        return cuentas.deUsuaria(usuaria.getId()).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    private void auditar(User usuaria, Account cuenta, String accion, String detalle) {
        if (usuaria.esAdmin()) events.sesionAdmin(cuenta.getId(), accion, detalle);
    }
}
