package com.alertamujer.resources.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de que la usuaria marcó a un recurso de ayuda desde la app.
 */
public class ResourceCall {

    private Long id;
    private Long emergencyResourceId;
    private Long userProfileId;
    private Long alertId;
    private Long deviceId;
    private String telephoneDialed;
    /** initiated si la app no indica otro. */
    private String status;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Reglas ──────────────────────────────────────────────────────

    /**
     * Llamada nueva: el número es el que marcó la app o, si no lo envía, el del recurso. La dueña
     * sale del token, nunca del body.
     */
    public static ResourceCall newOne(Long profile, EmergencyResource recurso, Long alertId, Long deviceId,
                                     String telefonoMarcado, String status, LocalDateTime now) {
        ResourceCall c = new ResourceCall();
        c.userProfileId = profile;
        c.emergencyResourceId = recurso.getId();
        c.alertId = alertId;
        c.deviceId = deviceId;
        c.telephoneDialed = telefonoMarcado != null && !telefonoMarcado.isBlank() ? telefonoMarcado.trim() : recurso.getTelephone();
        if (c.telephoneDialed == null) throw new IllegalArgumentException("El recurso no tiene teléfono");
        c.status = status != null ? status : "initiated";
        c.startedAt = now;
        return c;
    }

    public boolean esDe(Long profile) {
        return profile != null && Objects.equals(userProfileId, profile);
    }

    /** Resultado de la llamada: solo los datos que lleguen. */
    public void update(String newStatus, LocalDateTime end, Integer duracionSegundos) {
        if (newStatus != null) status = newStatus;
        if (end != null) endedAt = end;
        if (duracionSegundos != null) durationSeconds = duracionSegundos;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmergencyResourceId() { return emergencyResourceId; }
    public void setEmergencyResourceId(Long emergencyResourceId) { this.emergencyResourceId = emergencyResourceId; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public String getTelephoneDialed() { return telephoneDialed; }
    public void setTelephoneDialed(String telephoneDialed) { this.telephoneDialed = telephoneDialed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
