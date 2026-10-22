package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ExpireAlertsUseCase;
import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Expires the alerts that exceed the maximum duration configured by the administration. */
@Service
public class AlertExpirationService implements ExpireAlertsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlertExpirationService.class);

    private final AlertRepository repository;
    private final SettingsPort settings;
    private final AlertEventsPort events;

    // Last run, for the system status in the panel
    private volatile LocalDateTime lastRun;
    private volatile int expiredLastRun;

    public AlertExpirationService(AlertRepository repository, SettingsPort settings, AlertEventsPort events) {
        this.repository = repository;
        this.settings = settings;
        this.events = events;
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
        int minutes = settings.maxAlertDurationMinutes();
        if (minutes <= 0) return 0;
        LocalDateTime limit = now.minusMinutes(minutes);
        List<Alert> overdue = repository.active().stream().filter(a -> a.expired(limit)).toList();
        overdue.forEach(a -> a.expire(now));
        repository.saveAll(overdue);
        overdue.forEach(a -> events.alertEnded(a, now));
        return overdue.size();
    }
}
