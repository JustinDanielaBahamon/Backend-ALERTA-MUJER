package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Alerta SOS de una usuaria. Es el modelo del dominio: no conoce la base de datos ni Spring, y
 * guarda las reglas de una alerta (cómo cambia de estado, cuándo vence, cuántas se conservan).
 */
public class Alert {

    private Long id;
    private Long userProfileId;
    private Long deviceId;
    private String alertType = "main";
    private String activationMethod;
    /** active | resolved | cancelled | expired */
    private String status = ACTIVE;
    private String message;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** true = sus evidencias no se borran a los 60 días (lo marca la usuaria). */
    private boolean keepEvidence = false;

    public static final String ACTIVE = "active";
    /** Estados que la administración puede asignar. */
    public static final Set<String> STATUSES = Set.of(ACTIVE, "resolved", "cancelled", "expired");
    /** Cuántas alertas puede conservar cada usuaria (pruebas para una denuncia sin llenar la nube). */
    public static final int MAX_KEPT_ALERTS = 5;

    private static final Set<String> ENDED_STATUSES = Set.of("cancelled", "resolved", "finished", "ended");

    // ── Reglas ──────────────────────────────────────────────────────

    /** Estados en los que una alerta activa se considera terminada. */
    public static boolean isFinalStatus(String status) {
        return "resolved".equals(status) || "cancelled".equals(status) || "expired".equals(status);
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    public boolean esDe(Long profile) {
        return profile != null && Objects.equals(userProfileId, profile);
    }

    /** La alerta ya no está en curso: sus envíos pendientes no van a llegar. */
    public boolean ended() {
        return endedAt != null || cancelledAt != null
                || (status != null && ENDED_STATUSES.contains(status.toLowerCase()));
    }

    /** Sigue activa desde antes del límite (superó la duración máxima). */
    public boolean expired(LocalDateTime limit) {
        return isActive() && startedAt != null && startedAt.isBefore(limit);
    }

    /**
     * Cambios de la dueña: solo se aplican los campos enviados. Devuelve true si con ellos terminó
     * una alerta activa (hay que avisar a los contactos que la recibieron).
     */
    public boolean update(AlertChanges changes, LocalDateTime now) {
        String previous = status;
        if (changes.alertType() != null) alertType = changes.alertType();
        if (changes.activationMethod() != null) activationMethod = changes.activationMethod();
        if (changes.status() != null) status = changes.status();
        if (changes.message() != null) message = changes.message();
        if (changes.endedAt() != null) endedAt = changes.endedAt();
        if (changes.cancelledAt() != null) cancelledAt = changes.cancelledAt();
        if (!ACTIVE.equals(previous) || !isFinalStatus(status)) return false;
        markClosed(now);
        return true;
    }

    /**
     * Cambio de estado desde la administración: "resolved" guarda la hora de cierre y "cancelled" la
     * de cancelación. Devuelve true si terminó una alerta activa.
     */
    public boolean changeStatus(String newStatus, LocalDateTime now) {
        if (newStatus == null || !STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Estado no válido: " + newStatus);
        }
        String previous = status;
        status = newStatus;
        markClosed(now);
        return ACTIVE.equals(previous) && isFinalStatus(newStatus);
    }

    /** Superó la duración máxima configurada. */
    public void expire(LocalDateTime now) {
        status = "expired";
        endedAt = now;
    }

    /**
     * Marca (o desmarca) la alerta para conservar sus evidencias más allá de 60 días.
     * conservadas = cuántas alertas tiene ya marcadas la usuaria.
     */
    public void keepEvidence(boolean keep, long kept) {
        if (keep && !keepEvidence && kept >= MAX_KEPT_ALERTS) {
            throw new IllegalArgumentException("Puedes conservar hasta " + MAX_KEPT_ALERTS
                    + " alertas. Deja de conservar otra para guardar esta.");
        }
        keepEvidence = keep;
    }

    private void markClosed(LocalDateTime now) {
        if ("resolved".equals(status) && endedAt == null) endedAt = now;
        if ("cancelled".equals(status) && cancelledAt == null) cancelledAt = now;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getActivationMethod() { return activationMethod; }
    public void setActivationMethod(String activationMethod) { this.activationMethod = activationMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isKeepEvidence() { return keepEvidence; }
    public void setKeepEvidence(boolean keepEvidence) { this.keepEvidence = keepEvidence; }
}
