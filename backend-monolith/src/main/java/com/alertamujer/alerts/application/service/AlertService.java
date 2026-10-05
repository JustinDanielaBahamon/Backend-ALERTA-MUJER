package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
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

    public List<Alert> getAlertsByUserProfile(Long userProfileId) {
        return alertRepository.findByUserProfileId(userProfileId);
    }

    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    public Alert updateAlert(Long id, Alert alertDetails) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        
        if (alertDetails.getAlertType() != null) alert.setAlertType(alertDetails.getAlertType());
        if (alertDetails.getActivationMethod() != null) alert.setActivationMethod(alertDetails.getActivationMethod());
        if (alertDetails.getStatus() != null) alert.setStatus(alertDetails.getStatus());
        if (alertDetails.getMessage() != null) alert.setMessage(alertDetails.getMessage());
        if (alertDetails.getEndedAt() != null) alert.setEndedAt(alertDetails.getEndedAt());
        if (alertDetails.getCancelledAt() != null) alert.setCancelledAt(alertDetails.getCancelledAt());
        
        return alertRepository.save(alert);
    }

    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }
}
