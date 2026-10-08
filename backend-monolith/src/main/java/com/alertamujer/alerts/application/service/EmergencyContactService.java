package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;

    public EmergencyContactService(EmergencyContactRepository emergencyContactRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
    }

    public List<EmergencyContact> getAllContacts() {
        return emergencyContactRepository.findAll();
    }

    public Optional<EmergencyContact> getContactById(Long id) {
        return emergencyContactRepository.findById(id);
    }

    public Optional<EmergencyContact> getContactById(Long id, Long currentUserId) {
        return emergencyContactRepository.findById(id)
                .filter(contact -> contact.getUserProfileId().equals(currentUserId));
    }

    public List<EmergencyContact> getContactsByUserProfile(Long userProfileId) {
        return emergencyContactRepository.findByUserProfileId(userProfileId);
    }

    public EmergencyContact createContact(EmergencyContact contact, Long currentUserId) {
        if (!contact.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes crear contactos para otro usuario");
        }
        return emergencyContactRepository.save(contact);
    }

    public EmergencyContact updateContact(Long id, EmergencyContact contactDetails, Long currentUserId) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));
        
        if (!contact.getUserProfileId().equals(currentUserId)) {
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
        
        if (!contact.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes eliminar contactos de otro usuario");
        }
        
        emergencyContactRepository.deleteById(id);
    }
}
