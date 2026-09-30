package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
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

    public List<EmergencyContact> getContactsByUserProfile(Long userProfileId) {
        return emergencyContactRepository.findByUserProfileId(userProfileId);
    }

    public EmergencyContact createContact(EmergencyContact contact) {
        return emergencyContactRepository.save(contact);
    }

    public EmergencyContact updateContact(Long id, EmergencyContact contactDetails) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency contact not found"));
        
        contact.setContactName(contactDetails.getContactName());
        contact.setTelephone(contactDetails.getTelephone());
        contact.setEmail(contactDetails.getEmail());
        contact.setRelationship(contactDetails.getRelationship());
        
        return emergencyContactRepository.save(contact);
    }

    public void deleteContact(Long id) {
        emergencyContactRepository.deleteById(id);
    }
}
