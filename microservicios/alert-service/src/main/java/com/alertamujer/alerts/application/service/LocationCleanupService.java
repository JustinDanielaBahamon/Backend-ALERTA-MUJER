package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.CleanUpLocationsUseCase;
import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Borra lo del día a día a los 7 días y los recorridos de las alertas a los 30 (configurable). */
@Service
public class LocationCleanupService implements CleanUpLocationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(LocationCleanupService.class);

    private final LocationRepository repository;
    private final int daysWithoutAlert;
    private final int alertDays;

    public LocationCleanupService(LocationRepository repository,
                                      @Value("${locations.retention.days-without-alert:7}") int daysWithoutAlert,
                                      @Value("${locations.retention.days-with-alert:30}") int alertDays) {
        this.repository = repository;
        this.daysWithoutAlert = daysWithoutAlert;
        this.alertDays = alertDays;
    }

    @Override
    public int clear(LocalDateTime now) {
        int deleted = AuditSource.runAs("Tarea automática · Limpieza del historial de ubicaciones", () ->
                repository.deleteWithoutAlertBefore(now.minusDays(daysWithoutAlert))
                        + repository.deleteFromAlertsBefore(now.minusDays(alertDays)));
        if (deleted > 0) log.info("Ubicaciones antiguas eliminadas: {}", deleted);
        return deleted;
    }
}
