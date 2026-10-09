package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserProfileRepository userProfileRepository;

    public AlertService(AlertRepository alertRepository, UserProfileRepository userProfileRepository) {
        this.alertRepository = alertRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Optional<Alert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    public Optional<Alert> getAlertById(Long id, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        return alertRepository.findById(id)
                .filter(alert -> alert.getUserProfileId().equals(userProfile.getId()));
    }

    public List<Alert> getAlertsByUserProfile(Long userProfileId) {
        return alertRepository.findByUserProfileId(userProfileId);
    }

    public List<Alert> getAlertsByUserId(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));
        return alertRepository.findByUserProfileId(userProfile.getId());
    }

    public Alert createAlert(Alert alert, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        // Ignorar userProfileId del payload y usar el del JWT
        alert.setUserProfileId(userProfile.getId());
        return alertRepository.save(alert);
    }

    public Alert updateAlert(Long id, Alert alertDetails, Long currentUserId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        if (!alert.getUserProfileId().equals(userProfile.getId())) {
            throw new AccessDeniedException("No puedes modificar alertas de otro usuario");
        }
        
        if (alertDetails.getAlertType() != null) alert.setAlertType(alertDetails.getAlertType());
        if (alertDetails.getActivationMethod() != null) alert.setActivationMethod(alertDetails.getActivationMethod());
        if (alertDetails.getStatus() != null) alert.setStatus(alertDetails.getStatus());
        if (alertDetails.getMessage() != null) alert.setMessage(alertDetails.getMessage());
        if (alertDetails.getEndedAt() != null) alert.setEndedAt(alertDetails.getEndedAt());
        if (alertDetails.getCancelledAt() != null) alert.setCancelledAt(alertDetails.getCancelledAt());
        
        return alertRepository.save(alert);
    }

    public void deleteAlert(Long id, Long currentUserId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        if (!alert.getUserProfileId().equals(userProfile.getId())) {
            throw new AccessDeniedException("No puedes eliminar alertas de otro usuario");
        }
        
        alertRepository.deleteById(id);
    }
}
