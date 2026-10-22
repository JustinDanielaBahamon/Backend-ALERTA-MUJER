package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.in.AdministrarCuentasUseCase;
import com.alertamujer.identity.application.port.out.CifradoPort;
import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.application.port.out.EventosIdentidadPort;
import com.alertamujer.identity.application.port.out.PerfilRepositorio;
import com.alertamujer.identity.application.port.out.SesionRepositorio;
import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.CambiosCuenta;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Administración de cuentas: los datos y sus reglas viven en identity; el panel (admin) los usa. */
@Service
public class AdministracionCuentasService implements AdministrarCuentasUseCase {

    private final UsuariaRepositorio usuarias;
    private final CuentaRepositorio cuentas;
    private final PerfilRepositorio perfiles;
    private final SesionRepositorio sesiones;
    private final CifradoPort cifrado;
    private final EventosIdentidadPort events;

    public AdministracionCuentasService(UsuariaRepositorio usuarias, CuentaRepositorio cuentas, PerfilRepositorio perfiles,
                                        SesionRepositorio sesiones, CifradoPort cifrado, EventosIdentidadPort events) {
        this.usuarias = usuarias;
        this.cuentas = cuentas;
        this.perfiles = perfiles;
        this.sesiones = sesiones;
        this.cifrado = cifrado;
        this.events = events;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaDeUsuaria> listAll() {
        Map<Long, Account> porUsuaria = cuentas.todas().stream()
                .collect(Collectors.toMap(Account::getUserId, Function.identity(), (a, b) -> a));
        Map<Long, Long> perfilPorUsuaria = perfiles.todos().stream()
                .collect(Collectors.toMap(UserProfile::getUserId, UserProfile::getId, (a, b) -> a));
        return usuarias.todas().stream()
                .map(u -> new CuentaDeUsuaria(u, porUsuaria.get(u.getId()), perfilPorUsuaria.get(u.getId())))
                .toList();
    }

    @Override
    @Transactional
    public CuentaDeUsuaria actualizar(Long userId, CambiosCuenta cambios, Long quienActua) {
        User usuaria = existente(userId);
        Account cuenta = cuentas.deUsuaria(userId).orElse(null);
        if (cambios.accountStatus() != null) {
            if (!Account.ESTADOS.contains(cambios.accountStatus())) {
                throw new IllegalArgumentException("Invalid account status. Must be one of: active, inactive, blocked, pending");
            }
            if (userId.equals(quienActua) && "blocked".equals(cambios.accountStatus())) {
                throw new IllegalArgumentException("Cannot block your own account");
            }
            if (cuenta != null) {
                cuenta.cambiarEstado(cambios.accountStatus());
                cuentas.save(cuenta);
            }
        }
        if (cambios.email() != null) {
            Optional<User> otra = usuarias.porCorreo(cambios.email());
            if (otra.isPresent() && !otra.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
        }
        usuaria.actualizarDesdeAdministracion(cambios.firstName(), cambios.lastName(), cambios.telephone(), cambios.email());
        usuarias.save(usuaria);
        return new CuentaDeUsuaria(usuaria, cuenta, perfiles.deUsuaria(userId).map(UserProfile::getId).orElse(null));
    }

    @Override
    @Transactional
    public void anonimizar(Long userId) {
        User usuaria = existente(userId);
        usuaria.anonimizar();
        usuarias.save(usuaria);
        Optional<UserProfile> perfil = perfiles.deUsuaria(userId);
        perfil.ifPresent(p -> {
            p.setProfilePhotoUrl(null);
            perfiles.save(p);
        });
        cuentas.deUsuaria(userId).ifPresent(cuenta -> {
            // Contraseña aleatoria que nadie conoce: no se puede volver a entrar
            cuenta.desactivar(cifrado.cifrar(UUID.randomUUID().toString()));
            cuentas.save(cuenta);
            sesiones.activasDeCuenta(cuenta.getId()).forEach(s -> {
                s.revocar();
                sesiones.save(s);
            });
        });
        // Contactos, ubicaciones, recorridos y grabaciones viven en Alert y Evidence: se avisa para que los borren
        events.cuentaEliminada(userId, perfil.map(UserProfile::getId).orElse(null));
    }

    private User existente(Long userId) {
        return usuarias.find(userId).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }
}
