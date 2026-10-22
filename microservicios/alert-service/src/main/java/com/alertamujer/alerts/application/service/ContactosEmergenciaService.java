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
    private final IdentityPort identity;
    private final AjustesPort ajustes;
    private final CelularesPort phones;

    public ContactosEmergenciaService(ContactoRepositorio repository, IdentityPort identity, AjustesPort ajustes,
                                      CelularesPort phones) {
        this.repository = repository;
        this.identity = identity;
        this.ajustes = ajustes;
        this.phones = phones;
    }

    @Override
    public Optional<EmergencyContact> one(Long id, Long userId) {
        Long profile = identity.profileOf(userId).orElse(null);
        if (profile == null) return Optional.empty();
        return repository.find(id).filter(c -> c.esDe(profile));
    }

    @Override
    public List<EmergencyContact> ofUser(Long idPedido, Long userId) {
        Long own = profileOf(userId);
        if (idPedido.equals(userId) || idPedido.equals(own)) {
            return repository.ofUser(own);
        }
        // Los contactos son datos de terceros: ni la administración los consulta (auditoría S2)
        throw new AccessDeniedException("No puedes ver los contactos de otra usuaria");
    }

    @Override
    @Transactional
    public EmergencyContact create(DatosContacto data, Long userId) {
        // La dueña sale del JWT, nunca del body (evita IDOR)
        Long profile = profileOf(userId);
        int max = ajustes.maxContacts();
        if (repository.ofUser(profile).size() >= max) {
            throw new IllegalArgumentException("Puedes tener como máximo " + max + " contactos de emergencia");
        }
        return repository.save(EmergencyContact.created(profile, data));
    }

    @Override
    @Transactional
    public EmergencyContact update(Long id, DatosContacto data, Long userId) {
        EmergencyContact contacto = own(id, userId, "No puedes modificar contactos de otro usuario");
        contacto.update(data);
        return repository.save(contacto);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        own(id, userId, "No puedes eliminar contactos de otro usuario");
        repository.delete(id);
    }

    @Override
    public Map<Long, String> estadoApp(Long userId) {
        Map<Long, String> estados = new LinkedHashMap<>();
        for (EmergencyContact c : repository.ofUser(profileOf(userId))) {
            String email = c.getEmail();
            Optional<Long> account = email == null || email.isBlank()
                    ? Optional.empty()
                    : identity.usuariaPorCorreo(email.trim());
            estados.put(c.getId(), account
                    .map(id -> phones.phoneTokens(id).isEmpty() ? SIN_CELULAR : CON_APP)
                    .orElse(SIN_CUENTA));
        }
        return estados;
    }

    private Long profileOf(Long userId) {
        return identity.profileOf(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private EmergencyContact own(Long id, Long userId, String mensajeAjeno) {
        EmergencyContact contacto = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));
        if (!contacto.esDe(profileOf(userId))) throw new AccessDeniedException(mensajeAjeno);
        return contacto;
    }
}
