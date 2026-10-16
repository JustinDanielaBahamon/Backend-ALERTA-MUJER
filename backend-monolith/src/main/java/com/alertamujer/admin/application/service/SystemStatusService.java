package com.alertamujer.admin.application.service;

import com.alertamujer.alerts.api.AlertsApi;
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
    private final AlertsApi alertsApi;

    public SystemStatusService(JdbcTemplate jdbcTemplate, AlertsApi alertsApi) {
        this.jdbcTemplate = jdbcTemplate;
        this.alertsApi = alertsApi;
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

        long activas = baseDatos ? alertsApi.countActiveAlerts() : -1;
        AlertsApi.ExpirationStatus expiracion = alertsApi.expirationStatus();
        long arranqueMs = ManagementFactory.getRuntimeMXBean().getStartTime();

        return new SystemStatus(
                baseDatos,
                latenciaMs,
                !FirebaseApp.getApps().isEmpty(),
                activas,
                expiracion.lastCheck(),
                expiracion.lastExpiredCount(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(arranqueMs), ZoneId.systemDefault()),
                Runtime.version().feature());
    }

    /** pushNotifications indica si Firebase está configurado (sin él no salen notificaciones push). */
    public record SystemStatus(boolean database, long databaseLatencyMs, boolean pushNotifications,
                               long activeAlerts, LocalDateTime lastExpirationCheck, int lastExpiredCount,
                               LocalDateTime startedAt, int javaVersion) {}
}
