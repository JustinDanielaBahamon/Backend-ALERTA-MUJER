package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ContactosEmergenciaUseCase;
import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
import com.alertamujer.alerts.domain.model.DatosContacto;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Contactos de emergencia: cada usuaria administra los suyos, hasta el máximo configurado. */
@Service
public class ContactosEmergenciaService implements ContactosEmergenciaUseCase {

    private final ContactoRepositorio repositorio;
    private final IdentidadPort identidad;
    private final AjustesPort ajustes;
    private final CelularesPort celulares;

    public ContactosEmergenciaService(ContactoRepositorio repositorio, IdentidadPort identidad, AjustesPort ajustes,
                                      CelularesPort celulares) {
        this.repositorio = repositorio;
        this.identidad = identidad;
        this.ajustes = ajustes;
        this.celulares = celulares;
    }

    @Override
    public Optional<EmergencyContact> uno(Long id, Long userId) {
        Long perfil = identidad.perfilDe(userId).orElse(null);
        if (perfil == null) return Optional.empty();
        return repositorio.buscar(id).filter(c -> c.esDe(perfil));
    }

    @Override
    public List<EmergencyContact> deUsuaria(Long idPedido, Long userId) {
        Long propio = perfilDe(userId);
        if (idPedido.equals(userId) || idPedido.equals(propio)) {
            return repositorio.deLaUsuaria(propio);
        }
        // Los contactos son datos de terceros: ni la administración los consulta (auditoría S2)
        throw new AccessDeniedException("No puedes ver los contactos de otra usuaria");
    }

    @Override
    @Transactional
    public EmergencyContact crear(DatosContacto datos, Long userId) {
        // La dueña sale del JWT, nunca del body (evita IDOR)
        Long perfil = perfilDe(userId);
        int maximo = ajustes.maximoContactos();
        if (repositorio.deLaUsuaria(perfil).size() >= maximo) {
            throw new IllegalArgumentException("Puedes tener como máximo " + maximo + " contactos de emergencia");
        }
        return repositorio.guardar(EmergencyContact.nuevo(perfil, datos));
    }

    @Override
    @Transactional
    public EmergencyContact actualizar(Long id, DatosContacto datos, Long userId) {
        EmergencyContact contacto = propio(id, userId, "No puedes modificar contactos de otro usuario");
        contacto.actualizar(datos);
        return repositorio.guardar(contacto);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId) {
        propio(id, userId, "No puedes eliminar contactos de otro usuario");
        repositorio.eliminar(id);
    }

    @Override
    public Map<Long, String> estadoApp(Long userId) {
        Map<Long, String> estados = new LinkedHashMap<>();
        for (EmergencyContact c : repositorio.deLaUsuaria(perfilDe(userId))) {
            String correo = c.getEmail();
            Optional<Long> cuenta = correo == null || correo.isBlank()
                    ? Optional.empty()
                    : identidad.usuariaPorCorreo(correo.trim());
            estados.put(c.getId(), cuenta
                    .map(id -> celulares.tokensDeCelulares(id).isEmpty() ? SIN_CELULAR : CON_APP)
                    .orElse(SIN_CUENTA));
        }
        return estados;
    }

    private Long perfilDe(Long userId) {
        return identidad.perfilDe(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private EmergencyContact propio(Long id, Long userId, String mensajeAjeno) {
        EmergencyContact contacto = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));
        if (!contacto.esDe(perfilDe(userId))) throw new AccessDeniedException(mensajeAjeno);
        return contacto;
    }
}
