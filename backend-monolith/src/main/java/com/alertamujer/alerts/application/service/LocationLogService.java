package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.infrastructure.controller.dto.LocationLogRequest;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LocationLogService {

    private static final int MAX_LIMIT = 500;

    private final LocationLogRepository locationLogRepository;
    private final IdentityApi identityApi;
    private final AlertRepository alertRepository;
    private final DevicesApi devicesApi;

    public LocationLogService(LocationLogRepository locationLogRepository,
                              IdentityApi identityApi,
                              AlertRepository alertRepository,
                              DevicesApi devicesApi) {
        this.locationLogRepository = locationLogRepository;
        this.identityApi = identityApi;
        this.alertRepository = alertRepository;
        this.devicesApi = devicesApi;
    }

    // El JWT trae users.id; las ubicaciones se guardan por user_profile.id.
    private Long profileIdOf(Long userId) {
        return identityApi.requireProfileId(userId);
    }

    /**
     * Guarda la ubicación. Con varios celulares, la del día a día solo se guarda si viene del que la
     * usuaria usó más recientemente (null si se descarta); la de una alerta se guarda siempre.
     */
    public LocationLog saveLocation(LocationLogRequest request, Long currentUserId) {
        if (request.getLatitude() == null || request.getLongitude() == null
                || request.getLatitude().abs().compareTo(BigDecimal.valueOf(90)) > 0
                || request.getLongitude().abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Coordenadas inválidas");
        }
        Long profileId = profileIdOf(currentUserId);

        LocationLog log = new LocationLog();
        log.setUserProfileId(profileId);
        log.setLatitude(request.getLatitude());
        log.setLongitude(request.getLongitude());
        log.setAccuracy(request.getAccuracy());

        // Solo se asocia a la alerta si es de la misma usuaria
        if (request.getAlertId() != null) {
            alertRepository.findById(request.getAlertId())
                    .filter(a -> profileId.equals(a.getUserProfileId()))
                    .ifPresent(a -> log.setAlertId(a.getId()));
        }

        if (log.getAlertId() == null && !devicesApi.isMostRecentPhone(currentUserId, request.getDeviceId())) {
            return null;
        }

        // Puntos en cola offline: se respeta su hora si es de las últimas 24 h y no del futuro
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recordedAt = request.getRecordedAt();
        log.setRecordedAt(recordedAt != null && recordedAt.isAfter(now.minusHours(24)) && !recordedAt.isAfter(now)
                ? recordedAt : now);

        return locationLogRepository.save(log);
    }

    public List<LocationLog> getRecentForUser(Long targetUserId, int limit) {
        int size = Math.max(1, Math.min(limit, MAX_LIMIT));
        return locationLogRepository.findByUserProfileIdOrderByRecordedAtDesc(profileIdOf(targetUserId), PageRequest.of(0, size));
    }

    public Optional<LocationLog> getLatestForUser(Long targetUserId) {
        return locationLogRepository.findFirstByUserProfileIdOrderByRecordedAtDesc(profileIdOf(targetUserId));
    }

    /** Puntos de una alerta: solo la dueña de la alerta o un admin. */
    public List<LocationLog> getByAlert(Long alertId, Long currentUserId, boolean isAdmin) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        if (!isAdmin && !alert.getUserProfileId().equals(profileIdOf(currentUserId))) {
            throw new AccessDeniedException("No puedes ver ubicaciones de alertas de otra usuaria");
        }
        return locationLogRepository.findByAlertIdOrderByRecordedAtAsc(alertId);
    }
}
