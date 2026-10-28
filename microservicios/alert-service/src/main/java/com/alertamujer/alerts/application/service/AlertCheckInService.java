package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.alerts.application.port.in.AlertCheckInUseCase;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/** "Sigo en peligro" and the duress PIN, only on the user's own active alert. */
@Service
public class AlertCheckInService implements AlertCheckInUseCase {

    private final AlertRepository repository;
    private final IdentityPort identity;
    private final SettingsPort settings;
    private final AlertEventsPort events;
    private final int hardCapMinutes;

    public AlertCheckInService(AlertRepository repository, IdentityPort identity, SettingsPort settings,
                               AlertEventsPort events, @Value("${alerts.hard-cap-minutes:120}") int hardCapMinutes) {
        this.repository = repository;
        this.identity = identity;
        this.settings = settings;
        this.events = events;
        this.hardCapMinutes = hardCapMinutes;
    }

    @Override
    @Transactional
    public Alert extend(Long alertId, Long userId) {
        Alert alert = own(alertId, userId);
        int checkIn = checkInMinutes();
        alert.extend(LocalDateTime.now(), checkIn, hardCap(checkIn));
        return repository.save(alert);
    }

    @Override
    @Transactional
    public Alert duress(Long alertId, Long userId) {
        Alert alert = own(alertId, userId);
        LocalDateTime now = LocalDateTime.now();
        if (alert.markDuress(now)) {
            alert = repository.save(alert);
            events.alertWarning(alert, AlertWarningEvent.DURESS, now);
        }
        return alert;
    }

    /** Minutes between check-ins: the duration the administration configures (30 by default). */
    private int checkInMinutes() {
        int minutes = settings.maxAlertDurationMinutes();
        return minutes > 0 ? minutes : 30;
    }

    private Duration hardCap(int checkIn) {
        return Duration.ofMinutes(Math.max(hardCapMinutes, checkIn));
    }

    private Alert own(Long alertId, Long userId) {
        Long profile = identity.requiredProfile(userId);
        return repository.find(alertId).filter(a -> a.esDe(profile))
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertId));
    }
}
