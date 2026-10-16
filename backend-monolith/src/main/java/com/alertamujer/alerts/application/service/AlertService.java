package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.config.FcmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final IdentityApi identityApi;
    private final FcmService fcmService;

    @Autowired
    public AlertService(AlertRepository alertRepository,
                       IdentityApi identityApi,
                       FcmService fcmService) {
        this.alertRepository = alertRepository;
        this.identityApi = identityApi;
        this.fcmService = fcmService;
    }

    /** Alertas de la usuaria autenticada, de la más reciente a la más antigua. */
    public List<Alert> getMine(Long currentUserId) {
        Long profile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));
        return alertRepository.findByUserProfileId(profile).stream()
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .toList();
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Optional<Alert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    public Optional<Alert> getAlertById(Long id, Long currentUserId) {
        // Obtener el userProfile del usuario autenticado
        Long currentUserProfile = identityApi.findProfileId(currentUserId)
                .orElse(null);

        if (currentUserProfile == null) {
            return Optional.empty();
        }

        return alertRepository.findById(id)
                .filter(alert -> alert.getUserProfileId().equals(currentUserProfile));
    }

    public List<Alert> getAlertsByUserProfile(Long userProfileId, Long currentUserId) {
        // Obtener el userProfile del usuario autenticado
        Long currentUserProfile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for current user", currentUserId));

        // Solo la administración puede consultar alertas de otra usuaria
        UserSummary currentUser = identityApi.findUser(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));
        boolean isAdmin = currentUser.admin();

        // Los clientes envían el id de usuaria (users.id); también se acepta el id de perfil propio.
        // Ambos ids no coinciden necesariamente (en producción son distintos).
        if (userProfileId.equals(currentUserId) || userProfileId.equals(currentUserProfile)) {
            return alertRepository.findByUserProfileId(currentUserProfile);
        }
        // Solo el administrador puede ver alertas de otra usuaria: el id se interpreta como
        // users.id y, si no existe, como user_profile.id.
        if (!isAdmin) {
            throw new AccessDeniedException("No puedes ver alertas de otro usuario");
        }
        Long profileId = identityApi.findProfileId(userProfileId).orElse(userProfileId);
        return alertRepository.findByUserProfileId(profileId);
    }

    public Alert createAlert(Alert alert, Long currentUserId) {
        // El propietario se resuelve desde el JWT (users.id -> user_profile.id);
        // nunca se confía en el userProfileId que llega en el body (evita IDOR).
        Long profile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", currentUserId));
        alert.setUserProfileId(profile);
        return alertRepository.save(alert);
    }

    public Alert updateAlert(Long id, Alert alertDetails, Long currentUserId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));

        // Obtener el userProfile del usuario autenticado
        Long currentUserProfile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for current user", currentUserId));

        // Verificar ownership: comparar user_profile.id con user_profile.id
        if (!alert.getUserProfileId().equals(currentUserProfile)) {
            throw new AccessDeniedException("No puedes modificar alertas de otro usuario");
        }

        if (alertDetails.getAlertType() != null) alert.setAlertType(alertDetails.getAlertType());
        if (alertDetails.getActivationMethod() != null) alert.setActivationMethod(alertDetails.getActivationMethod());
        if (alertDetails.getStatus() != null) alert.setStatus(alertDetails.getStatus());
        if (alertDetails.getMessage() != null) alert.setMessage(alertDetails.getMessage());
        if (alertDetails.getEndedAt() != null) alert.setEndedAt(alertDetails.getEndedAt());
        if (alertDetails.getCancelledAt() != null) alert.setCancelledAt(alertDetails.getCancelledAt());

        // Cuando una alerta se actualiza a 'resolved' o 'cancelled', enviar notificaciones
        if (alertDetails.getStatus() != null) {
            String newStatus = alertDetails.getStatus();
            if (newStatus.equals("resolved") || newStatus.equals("cancelled")) {
                // Notificar cuando se resuelve o cancela una alerta
                fcmService.notifyAlertTriggered(alert.getId(), alert.getUserProfileId());
            }
        }

        return alertRepository.save(alert);
    }

    public void deleteAlert(Long id, Long currentUserId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));

        // Obtener el userProfile del usuario autenticado
        Long currentUserProfile = identityApi.findProfileId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for current user", currentUserId));

        // Verificar ownership: comparar user_profile.id con user_profile.id
        if (!alert.getUserProfileId().equals(currentUserProfile)) {
            throw new AccessDeniedException("No puedes eliminar alertas de otro usuario");
        }

        alertRepository.deleteById(id);
    }

    /**
     * Notificar cuando se activa una alerta - enviar push a todos los dispositivos del usuario
     */
    @Transactional
    public void notifyAlertTriggered(Long alertId, Long userProfileId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));

        // Obtener dispositivos del usuario
        List<String> fcmTokens = fcmService.getFcmTokensByUser(userProfileId);

        if (!fcmTokens.isEmpty()) {
            try {
                fcmService.sendBatchNotification(fcmTokens, "Alerta activada",
                        "Se ha activado una alerta de emergencia N°" + alertId,
                        String.valueOf(alertId), "triggered");
            } catch (Exception e) {
                // Log error pero no lanzar excepción para no bloquear el flujo
                e.printStackTrace();
            }
        }
    }

    /**
     * Notificar actualización de ubicación en tiempo real
     */
    @Transactional
    public void notifyLocationUpdate(Long alertId, Long userProfileId, double latitude, double longitude) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));

        // Obtener tokens FCM de los dispositivos del usuario
        List<String> fcmTokens = fcmService.getFcmTokensByUser(userProfileId);

        if (!fcmTokens.isEmpty()) {
            try {
                // Enviar notificación al primer dispositivo
                fcmService.sendLocationUpdateNotification(String.valueOf(alertId), fcmTokens.get(0), latitude, longitude);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}