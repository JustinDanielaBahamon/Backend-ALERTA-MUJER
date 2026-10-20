package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.AdministrarCuentasUseCase;
import com.alertamujer.identity.application.port.in.MiCuentaUseCase;
import com.alertamujer.identity.application.port.out.CifradoPort;
import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.application.port.out.EventosIdentidadPort;
import com.alertamujer.identity.application.port.out.PerfilRepositorio;
import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.CambiosUsuaria;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.FotoDataUri;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Datos, foto y contraseña de la persona en sesión. */
@Service
public class MiCuentaService implements MiCuentaUseCase {

    private final UsuariaRepositorio usuarias;
    private final PerfilRepositorio perfiles;
    private final CuentaRepositorio cuentas;
    private final CifradoPort cifrado;
    private final EventosIdentidadPort eventos;
    private final AdministrarCuentasUseCase administracion;

    public MiCuentaService(UsuariaRepositorio usuarias, PerfilRepositorio perfiles, CuentaRepositorio cuentas,
                           CifradoPort cifrado, EventosIdentidadPort eventos, AdministrarCuentasUseCase administracion) {
        this.usuarias = usuarias;
        this.perfiles = perfiles;
        this.cuentas = cuentas;
        this.cifrado = cifrado;
        this.eventos = eventos;
        this.administracion = administracion;
    }

    @Override
    public UsuariaConFoto mia(Long userId) {
        return new UsuariaConFoto(existente(userId), foto(userId));
    }

    @Override
    @Transactional
    public UsuariaConFoto actualizar(Long userId, CambiosUsuaria cambios) {
        User usuaria = existente(userId);
        // La foto se valida antes de guardar nada: "" o null la quitan
        if (cambios.profilePhoto() != null) {
            String valida = FotoDataUri.validar(cambios.profilePhoto().orElse(null));
            perfiles.deUsuaria(userId).ifPresent(p -> {
                p.setProfilePhotoUrl(valida);
                perfiles.guardar(p);
            });
        }
        usuaria.actualizarDatos(cambios);
        return new UsuariaConFoto(usuarias.guardar(usuaria), foto(userId));
    }

    @Override
    @Transactional
    public void cambiarContrasena(Long userId, String actual, String nueva) {
        Account cuenta = cuentas.deUsuaria(userId).orElseThrow(() -> new IllegalArgumentException("La cuenta no existe"));
        if (actual == null || !cifrado.coincide(actual, cuenta.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        Account.validarContrasenaNueva(nueva);
        if (cifrado.coincide(nueva, cuenta.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
        }
        cuenta.cambiarContrasena(cifrado.cifrar(nueva));
        cuentas.guardar(cuenta);
        if (usuarias.buscar(userId).map(User::esAdmin).orElse(false)) {
            eventos.sesionAdmin(cuenta.getId(), AdminSessionEvent.PASSWORD_CHANGED, null);
        }
    }

    @Override
    @Transactional
    public void eliminarMiCuenta(Long userId, String confirmacion) {
        if (confirmacion == null || !CONFIRMACION_ELIMINAR.equalsIgnoreCase(confirmacion.trim())) {
            throw new IllegalArgumentException("Escribe " + CONFIRMACION_ELIMINAR + " para confirmar");
        }
        if (existente(userId).esAdmin()) {
            throw new IllegalArgumentException(
                    "Las cuentas de administración se eliminan desde el panel, con la aprobación de otra administradora");
        }
        // Mismo borrado que aprueba la administración: datos personales fuera, sesión cerrada y aviso a los demás servicios
        administracion.anonimizar(userId);
    }

    private User existente(Long userId) {
        return usuarias.buscar(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private String foto(Long userId) {
        return perfiles.deUsuaria(userId).map(UserProfile::getProfilePhotoUrl).orElse(null);
    }
}
