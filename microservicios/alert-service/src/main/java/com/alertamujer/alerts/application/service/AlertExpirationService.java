package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.alerts.application.port.in.ExpireAlertsUseCase;
import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Check-in of the active alerts. When the duration configured by the administration (30 min) passes
 * without her saying she is still in danger, the alert is NOT closed: if she cannot answer she may be
 * in danger, so her contacts are told she does not respond and it goes on. It is only closed at the
 * absolute limit (2 h by default).
 */
@Service
public class AlertExpirationService implements ExpireAlertsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlertExpirationService.class);

    private final AlertRepository repository;
    private final SettingsPort settings;
    private final AlertEventsPort events;

    // Last run, for the system status in the panel
    private volatile LocalDateTime lastRun;
    private volatile int expiredLastRun;

    private final int hardCapMinutes;

    public AlertExpirationService(AlertRepository repository, SettingsPort settings, AlertEventsPort events,
                                  @Value("${alerts.hard-cap-minutes:120}") int hardCapMinutes) {
        this.repository = repository;
        this.settings = settings;
        this.events = events;
        this.hardCapMinutes = hardCapMinutes;
    }

    @Override
    public int review(LocalDateTime now) {
        // saveAll opens the transaction inside AuditSource.runAs, so the audit records the source
        int expired = AuditSource.runAs("Tarea automática · Expiración de alertas", () -> expireOverdue(now));
        lastRun = now;
        expiredLastRun = expired;
        if (expired > 0) log.info("Alertas expiradas por superar la duración máxima: {}", expired);
        return expired;
    }

    @Override
    public LocalDateTime lastRun() {
        return lastRun;
    }

    @Override
    public int expiredLastRun() {
        return expiredLastRun;
    }

    int expireOverdue(LocalDateTime now) {
        int checkIn = settings.maxAlertDurationMinutes();
        if (checkIn <= 0) return 0;
        Duration hardCap = Duration.ofMinutes(Math.max(hardCapMinutes, checkIn));
        List<Alert> expired = new ArrayList<>();
        List<Alert> unanswered = new ArrayList<>();
        for (Alert a : repository.active()) {
            if (a.overHardLimit(now, hardCap)) {
                a.expire(now);
                expired.add(a);
            } else if (a.unanswered(now, checkIn)) {
                a.markUnanswered(now);
                unanswered.add(a);
            }
        }
        List<Alert> changed = new ArrayList<>(expired);
        changed.addAll(unanswered);
        repository.saveAll(changed);
        expired.forEach(a -> events.alertEnded(a, now));
        unanswered.forEach(a -> events.alertWarning(a, AlertWarningEvent.UNANSWERED, now));
        return expired.size();
    }
}
