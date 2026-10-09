package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public AlertService(AlertRepository alertRepository, 
                       UserProfileRepository userProfileRepository,
                       UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Optional<Alert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    public Optional<Alert> getAlertById(Long id, Long currentUserId) {
        return alertRepository.findById(id)
                .filter(alert -> alert.getUserProfileId().equals(currentUserId));
    }

    public List<Alert> getAlertsByUserProfile(Long userProfileId, Long currentUserId) {
        // Obtener el userProfile del usuario autenticado
        UserProfile currentUserProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for current user", currentUserId));
        
        // Obtener información del usuario para verificar si es ADMIN
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));
        
        boolean isAdmin = currentUser.getRole().getName().equals("administrator");
        
        // Permitir acceso SOLO si:
        // 1. El usuario autenticado es dueño del userProfile solicitado
        // 2. O el usuario autenticado tiene rol ADMIN
        if (!currentUserProfile.getId().equals(userProfileId) && !isAdmin) {
            throw new AccessDeniedException("No puedes ver alertas de otro usuario");
        }
        
        return alertRepository.findByUserProfileId(userProfileId);
    }

    public Alert createAlert(Alert alert, Long currentUserId) {
        // El propietario se resuelve desde el JWT (users.id -> user_profile.id);
        // nunca se confía en el userProfileId que llega en el body (evita IDOR).
        UserProfile profile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));
        alert.setUserProfileId(profile.getId());
        return alertRepository.save(alert);
    }

    public Alert updateAlert(Long id, Alert alertDetails, Long currentUserId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));

        if (!alert.getUserProfileId().equals(currentUserId)) {
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

        if (!alert.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes eliminar alertas de otro usuario");
        }

        alertRepository.deleteById(id);
    }
}
