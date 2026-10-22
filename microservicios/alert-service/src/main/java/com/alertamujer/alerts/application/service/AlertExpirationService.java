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

/** Expira las alertas que superan la duración máxima configurada por la administración. */
@Service
public class AlertExpirationService implements ExpireAlertsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlertExpirationService.class);

    private final AlertRepository repository;
    private final SettingsPort settings;
    private final AlertEventsPort events;

    // Última ejecución, para el estado del sistema en el panel
    private volatile LocalDateTime lastRun;
    private volatile int expiredLastRun;

    public AlertExpirationService(AlertRepository repository, SettingsPort settings, AlertEventsPort events) {
        this.repository = repository;
        this.settings = settings;
        this.events = events;
    }

    @Override
    public int review(LocalDateTime now) {
        // guardarTodas abre la transacción dentro de AuditSource.con, así la auditoría registra el origen
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
