package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GPS point of a user: day-to-day or from an alert route.
 */
public class LocationLog {

    private Long id;
    private Long userProfileId;
    /** Null for day-to-day locations. */
    private Long alertId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracy;
    private LocalDateTime recordedAt = LocalDateTime.now();

    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    // ── Reglas ──────────────────────────────────────────────────────

    public static LocationLog created(Long profile, BigDecimal latitude, BigDecimal longitude, BigDecimal precision) {
        if (latitude == null || longitude == null
                || latitude.abs().compareTo(MAX_LATITUDE) > 0 || longitude.abs().compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("Coordenadas inválidas");
        }
        LocationLog l = new LocationLog();
        l.userProfileId = profile;
        l.latitude = latitude;
        l.longitude = longitude;
        l.accuracy = precision;
        return l;
    }

    public boolean belongsToAlert() {
        return alertId != null;
    }

    /** Points queued while offline: their time is respected if within the last 24 h and not in the future. */
    public void recordTime(LocalDateTime sent, LocalDateTime now) {
        recordedAt = sent != null && sent.isAfter(now.minusHours(24)) && !sent.isAfter(now) ? sent : now;
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
