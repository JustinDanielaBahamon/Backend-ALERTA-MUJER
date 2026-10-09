package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final UserProfileRepository userProfileRepository;

    public EmergencyContactService(EmergencyContactRepository emergencyContactRepository, UserProfileRepository userProfileRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<EmergencyContact> getAllContacts() {
        return emergencyContactRepository.findAll();
    }

    public Optional<EmergencyContact> getContactById(Long id) {
        return emergencyContactRepository.findById(id);
    }

    public Optional<EmergencyContact> getContactById(Long id, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        return emergencyContactRepository.findById(id)
                .filter(contact -> contact.getUserProfileId().equals(userProfile.getId()));
    }

    public List<EmergencyContact> getContactsByUserProfile(Long userProfileId) {
        return emergencyContactRepository.findByUserProfileId(userProfileId);
    }

    public List<EmergencyContact> getContactsByUserId(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));
        return emergencyContactRepository.findByUserProfileId(userProfile.getId());
    }

    public EmergencyContact createContact(EmergencyContact contact, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        // Ignorar userProfileId del payload y usar el del JWT
        contact.setUserProfileId(userProfile.getId());
        return emergencyContactRepository.save(contact);
    }

    public EmergencyContact updateContact(Long id, EmergencyContact contactDetails, Long currentUserId) {
        EmergencyContact contact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", id));
        
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        if (!contact.getUserProfileId().equals(userProfile.getId())) {
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
        
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        if (!contact.getUserProfileId().equals(userProfile.getId())) {
            throw new AccessDeniedException("No puedes eliminar contactos de otro usuario");
        }
        
        emergencyContactRepository.deleteById(id);
    }
}
