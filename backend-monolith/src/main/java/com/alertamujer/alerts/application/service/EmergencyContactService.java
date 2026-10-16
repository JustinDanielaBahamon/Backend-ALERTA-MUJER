package com.alertamujer.alerts.application.service;

import com.alertamujer.admin.application.service.AdminSettingsService;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final IdentityApi identityApi;
    private final AdminSettingsService settingsService;

    public EmergencyContactService(EmergencyContactRepository emergencyContactRepository,
                                   IdentityApi identityApi,
                                   AdminSettingsService settingsService) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.identityApi = identityApi;
        this.settingsService = settingsService;
    }

    public List<EmergencyContact> getAllContacts() {
        return emergencyContactRepository.findAll();
    }

    public Optional<EmergencyContact> getContactById(Long id) {
        return emergencyContactRepository.findById(id);
    }

    public Optional<EmergencyContact> getContactById(Long id, Long currentUserId) {
        Long profile = identityApi.findProfileId(currentUserId)
                .orElse(null);
        if (profile == null) return Optional.empty();

        return emergencyContactRepository.findById(id)
                .filter(contact -> profile.equals(contact.getUserProfileId()));
    }

    /**
     * Contactos de una usuaria. Los clientes envían el id de usuaria (users.id); también se acepta
     * el id de perfil propio. Una usuaria solo ve sus contactos; el administrador puede ver los de
     * cualquiera (el id se interpreta como users.id y, si no existe, como user_profile.id).
     */
    public List<EmergencyContact> getContactsForUser(Long requestedId, Long currentUserId, boolean isAdmin) {
        Long own = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));
        if (requestedId.equals(currentUserId) || requestedId.equals(own)) {
            return emergencyContactRepository.findByUserProfileId(own);
        }
        if (!isAdmin) {
            throw new AccessDeniedException("No puedes ver los contactos de otra usuaria");
        }
        Long profileId = identityApi.findProfileId(requestedId)
                .orElse(requestedId);
        return emergencyContactRepository.findByUserProfileId(profileId);
    }

    public EmergencyContact createContact(EmergencyContact contact, Long currentUserId) {
        // Resolver userProfileId desde el JWT — nunca confiar en el body (evita IDOR)
        Long profile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));
        int maximo = settingsService.maximoContactos();
        if (emergencyContactRepository.findByUserProfileId(profile).size() >= maximo) {
            throw new IllegalArgumentException("Puedes tener como máximo " + maximo + " contactos de emergencia");
        }
        contact.setUserProfileId(profile);
        return emergencyContactRepository.save(contact);
    }

    public EmergencyContact updateContact(Long id, EmergencyContact contactDetails, Long currentUserId) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));

        Long profile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));

        if (!profile.equals(contact.getUserProfileId())) {
            throw new AccessDeniedException("No puedes modificar contactos de otro usuario");
        }

        contact.setContactName(contactDetails.getContactName());
        contact.setTelephone(contactDetails.getTelephone());
        contact.setEmail(contactDetails.getEmail());
        contact.setRelationship(contactDetails.getRelationship());

        return emergencyContactRepository.save(contact);
    }

    public void deleteContact(Long id, Long currentUserId) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));

        Long profile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));

        if (!profile.equals(contact.getUserProfileId())) {
            throw new AccessDeniedException("No puedes eliminar contactos de otro usuario");
        }

        emergencyContactRepository.deleteById(id);
    }
}
