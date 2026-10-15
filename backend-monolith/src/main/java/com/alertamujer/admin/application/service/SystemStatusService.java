package com.alertamujer.admin.application.service;

import com.alertamujer.alerts.application.service.AlertExpirationJob;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.google.firebase.FirebaseApp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Estado real de las piezas del backend que el panel muestra en Configuración. */
@Service
public class SystemStatusService {

    private final JdbcTemplate jdbcTemplate;
    private final AlertRepository alertRepository;
    private final AlertExpirationJob alertExpirationJob;

    public SystemStatusService(JdbcTemplate jdbcTemplate, AlertRepository alertRepository,
                               AlertExpirationJob alertExpirationJob) {
        this.jdbcTemplate = jdbcTemplate;
        this.alertRepository = alertRepository;
        this.alertExpirationJob = alertExpirationJob;
    }

    public SystemStatus estado() {
        long inicio = System.nanoTime();
        boolean baseDatos;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            baseDatos = true;
        } catch (RuntimeException e) {
            baseDatos = false;
        }
        long latenciaMs = Duration.ofNanos(System.nanoTime() - inicio).toMillis();

        long activas = baseDatos ? alertRepository.findByStatus("active").size() : -1;
        long arranqueMs = ManagementFactory.getRuntimeMXBean().getStartTime();

        return new SystemStatus(
                baseDatos,
                latenciaMs,
                !FirebaseApp.getApps().isEmpty(),
                activas,
                alertExpirationJob.getUltimaRevision(),
                alertExpirationJob.getExpiradasUltimaRevision(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(arranqueMs), ZoneId.systemDefault()),
                Runtime.version().feature());
    }

    /** pushNotifications indica si Firebase está configurado (sin él no salen notificaciones push). */
    public record SystemStatus(boolean database, long databaseLatencyMs, boolean pushNotifications,
                               long activeAlerts, LocalDateTime lastExpirationCheck, int lastExpiredCount,
                               LocalDateTime startedAt, int javaVersion) {}
}
