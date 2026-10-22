package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ContactosEmergenciaUseCase;
import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.application.port.out.IdentityPort;
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

    private final ContactoRepositorio repository;
    private final IdentityPort identidad;
    private final AjustesPort ajustes;
    private final CelularesPort celulares;

    public ContactosEmergenciaService(ContactoRepositorio repository, IdentityPort identidad, AjustesPort ajustes,
                                      CelularesPort celulares) {
        this.repository = repository;
        this.identidad = identidad;
        this.ajustes = ajustes;
        this.celulares = celulares;
    }

    @Override
    public Optional<EmergencyContact> uno(Long id, Long userId) {
        Long perfil = identidad.profileOf(userId).orElse(null);
        if (perfil == null) return Optional.empty();
        return repository.find(id).filter(c -> c.esDe(perfil));
    }

    @Override
    public List<EmergencyContact> deUsuaria(Long idPedido, Long userId) {
        Long propio = profileOf(userId);
        if (idPedido.equals(userId) || idPedido.equals(propio)) {
            return repository.ofUser(propio);
        }
        // Los contactos son datos de terceros: ni la administración los consulta (auditoría S2)
        throw new AccessDeniedException("No puedes ver los contactos de otra usuaria");
    }

    @Override
    @Transactional
    public EmergencyContact create(DatosContacto data, Long userId) {
        // La dueña sale del JWT, nunca del body (evita IDOR)
        Long perfil = profileOf(userId);
        int maximo = ajustes.maximoContactos();
        if (repository.ofUser(perfil).size() >= maximo) {
            throw new IllegalArgumentException("Puedes tener como máximo " + maximo + " contactos de emergencia");
        }
        return repository.save(EmergencyContact.nuevo(perfil, data));
    }

    @Override
    @Transactional
    public EmergencyContact actualizar(Long id, DatosContacto data, Long userId) {
        EmergencyContact contacto = propio(id, userId, "No puedes modificar contactos de otro usuario");
        contacto.actualizar(data);
        return repository.save(contacto);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        propio(id, userId, "No puedes eliminar contactos de otro usuario");
        repository.delete(id);
    }

    @Override
    public Map<Long, String> estadoApp(Long userId) {
        Map<Long, String> estados = new LinkedHashMap<>();
        for (EmergencyContact c : repository.ofUser(profileOf(userId))) {
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

    private Long profileOf(Long userId) {
        return identidad.profileOf(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private EmergencyContact propio(Long id, Long userId, String mensajeAjeno) {
        EmergencyContact contacto = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));
        if (!contacto.esDe(profileOf(userId))) throw new AccessDeniedException(mensajeAjeno);
        return contacto;
    }
}
