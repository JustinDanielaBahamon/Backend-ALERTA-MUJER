package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NewLocation;
import com.alertamujer.alerts.application.port.in.LocationsUseCase;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Historial de ubicaciones: del día a día (solo del celular principal) y de cada alerta. */
@Service
public class LocationsService implements LocationsUseCase {

    private static final int MAX_LIMIT = 500;

    private final LocationRepository repository;
    private final AlertRepository alerts;
    private final IdentityPort identity;
    private final PhonesPort phones;

    public LocationsService(LocationRepository repository, AlertRepository alerts,
                              IdentityPort identity, PhonesPort phones) {
        this.repository = repository;
        this.alerts = alerts;
        this.identity = identity;
        this.phones = phones;
    }

    @Override
    public Optional<LocationLog> save(NewLocation newOne, Long userId) {
        Long profile = identity.requiredProfile(userId);
        LocationLog location = LocationLog.created(profile, newOne.latitude(), newOne.longitude(), newOne.accuracy());
        // Solo se asocia a la alerta si es de la misma usuaria
        if (newOne.alertId() != null) {
            alerts.find(newOne.alertId()).filter(a -> a.esDe(profile)).ifPresent(a -> location.setAlertId(a.getId()));
        }
        if (!location.belongsToAlert() && !phones.isTheNewest(userId, newOne.deviceId())) {
            return Optional.empty();
        }
        location.recordTime(newOne.recordedAt(), LocalDateTime.now());
        return Optional.of(repository.save(location));
    }

    @Override
    public List<LocationLog> recent(Long userId, int limit) {
        return repository.recent(identity.requiredProfile(userId), Math.max(1, Math.min(limit, MAX_LIMIT)));
    }

    @Override
    public Optional<LocationLog> latest(Long userId) {
        return repository.latest(identity.requiredProfile(userId));
    }

    @Override
    public List<LocationLog> ofAlert(Long alertId, Long userId, boolean admin) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        boolean own = alert.esDe(identity.requiredProfile(userId));
        // La administración solo ve el recorrido de alertas activas o atendidas (auditoría S2):
        // las canceladas o vencidas pudieron ser una falsa alarma y no se revisan
        boolean administrationCan = admin && (alert.isActive() || "resolved".equals(alert.getStatus()));
        if (!own && !administrationCan) {
            throw new AccessDeniedException("No puedes ver ubicaciones de esta alerta");
        }
        return repository.ofAlert(alertId);
    }

    @Override
    public List<LocationLog> trip(Long alertId) {
        return repository.ofAlert(alertId);
    }
}
