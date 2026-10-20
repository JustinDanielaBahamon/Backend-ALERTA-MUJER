package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Punto GPS de una usuaria: del día a día o del recorrido de una alerta.
 */
public class LocationLog {

    private Long id;
    private Long userProfileId;
    /** Null en las ubicaciones del día a día. */
    private Long alertId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracy;
    private LocalDateTime recordedAt = LocalDateTime.now();

    private static final BigDecimal MAX_LATITUD = BigDecimal.valueOf(90);
    private static final BigDecimal MAX_LONGITUD = BigDecimal.valueOf(180);

    // ── Reglas ──────────────────────────────────────────────────────

    public static LocationLog nuevo(Long perfil, BigDecimal latitud, BigDecimal longitud, BigDecimal precision) {
        if (latitud == null || longitud == null
                || latitud.abs().compareTo(MAX_LATITUD) > 0 || longitud.abs().compareTo(MAX_LONGITUD) > 0) {
            throw new IllegalArgumentException("Coordenadas inválidas");
        }
        LocationLog l = new LocationLog();
        l.userProfileId = perfil;
        l.latitude = latitud;
        l.longitude = longitud;
        l.accuracy = precision;
        return l;
    }

    public boolean esDeAlerta() {
        return alertId != null;
    }

    /** Puntos en cola sin conexión: se respeta su hora si es de las últimas 24 h y no del futuro. */
    public void registrarHora(LocalDateTime enviada, LocalDateTime ahora) {
        recordedAt = enviada != null && enviada.isAfter(ahora.minusHours(24)) && !enviada.isAfter(ahora) ? enviada : ahora;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getAccuracy() { return accuracy; }
    public void setAccuracy(BigDecimal accuracy) { this.accuracy = accuracy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
