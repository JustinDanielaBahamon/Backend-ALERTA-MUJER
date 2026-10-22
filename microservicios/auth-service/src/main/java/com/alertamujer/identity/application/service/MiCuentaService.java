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
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.FotoDataUri;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Datos, foto y contraseña de la persona en sesión. */
@Service
public class MiCuentaService implements MiCuentaUseCase {

    private final UsuariaRepositorio usuarias;
    private final PerfilRepositorio perfiles;
    private final CuentaRepositorio cuentas;
    private final CifradoPort cifrado;
    private final EventosIdentidadPort events;
    private final AdministrarCuentasUseCase administracion;

    public MiCuentaService(UsuariaRepositorio usuarias, PerfilRepositorio perfiles, CuentaRepositorio cuentas,
                           CifradoPort cifrado, EventosIdentidadPort events, AdministrarCuentasUseCase administracion) {
        this.usuarias = usuarias;
        this.perfiles = perfiles;
        this.cuentas = cuentas;
        this.cifrado = cifrado;
        this.events = events;
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
        cambios = sinRepetidos(usuaria, cambios);
        // La foto se valida antes de guardar nada: "" o null la quitan
        if (cambios.profilePhoto() != null) {
            String valida = FotoDataUri.validate(cambios.profilePhoto().orElse(null));
            perfiles.deUsuaria(userId).ifPresent(p -> {
                p.setProfilePhotoUrl(valida);
                perfiles.save(p);
            });
        }
        usuaria.actualizarDatos(cambios);
        return new UsuariaConFoto(usuarias.save(usuaria), foto(userId));
    }

    /**
     * El teléfono y el documento identifican a una sola persona (así se registró): al cambiarlos o al
     * completarlos (cuentas creadas con Google) no pueden ser los de otra cuenta.
     */
    private CambiosUsuaria sinRepetidos(User usuaria, CambiosUsuaria c) {
        Optional<String> telefono = c.telephone() == null ? null : c.telephone().map(User::limpiarTelefono);
        Optional<String> documento = c.documentNumber() == null ? null : c.documentNumber().map(User::vacioANull);
        Map<String, String> repetidos = new LinkedHashMap<>();
        if (telefono != null && telefono.isPresent() && !telefono.get().equals(usuaria.getTelephone())
                && usuarias.existeTelefono(telefono.get())) {
            repetidos.put("telephone", "Este número de teléfono ya está registrado");
        }
        if (documento != null && documento.isPresent() && !documento.get().equals(usuaria.getDocumentNumber())
                && usuarias.existeDocumento(documento.get())) {
            repetidos.put("documentNumber", "Este número de documento ya está registrado");
        }
        if (!repetidos.isEmpty()) throw new DuplicateFieldsException(repetidos);
        return new CambiosUsuaria(c.firstName(), c.lastName(), telefono, documento, c.documentType(), c.birthdate(),
                c.profilePhoto());
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
        cuentas.save(cuenta);
        if (usuarias.find(userId).map(User::esAdmin).orElse(false)) {
            events.sesionAdmin(cuenta.getId(), AdminSessionEvent.PASSWORD_CHANGED, null);
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
        return usuarias.find(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private String foto(Long userId) {
        return perfiles.deUsuaria(userId).map(UserProfile::getProfilePhotoUrl).orElse(null);
    }
}
