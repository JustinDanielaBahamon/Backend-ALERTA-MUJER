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
    private String status = ACTIVA;
    private String message;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** true = sus evidencias no se borran a los 60 días (lo marca la usuaria). */
    private boolean keepEvidence = false;

    public static final String ACTIVA = "active";
    /** Estados que la administración puede asignar. */
    public static final Set<String> ESTADOS = Set.of(ACTIVA, "resolved", "cancelled", "expired");
    /** Cuántas alertas puede conservar cada usuaria (pruebas para una denuncia sin llenar la nube). */
    public static final int MAX_ALERTAS_CONSERVADAS = 5;

    private static final Set<String> ESTADOS_TERMINADA = Set.of("cancelled", "resolved", "finished", "ended");

    // ── Reglas ──────────────────────────────────────────────────────

    /** Estados en los que una alerta activa se considera terminada. */
    public static boolean esEstadoFinal(String estado) {
        return "resolved".equals(estado) || "cancelled".equals(estado) || "expired".equals(estado);
    }

    public boolean estaActiva() {
        return ACTIVA.equals(status);
    }

    public boolean esDe(Long perfil) {
        return perfil != null && Objects.equals(userProfileId, perfil);
    }

    /** La alerta ya no está en curso: sus envíos pendientes no van a llegar. */
    public boolean terminada() {
        return endedAt != null || cancelledAt != null
                || (status != null && ESTADOS_TERMINADA.contains(status.toLowerCase()));
    }

    /** Sigue activa desde antes del límite (superó la duración máxima). */
    public boolean vencida(LocalDateTime limite) {
        return estaActiva() && startedAt != null && startedAt.isBefore(limite);
    }

    /**
     * Cambios de la dueña: solo se aplican los campos enviados. Devuelve true si con ellos terminó
     * una alerta activa (hay que avisar a los contactos que la recibieron).
     */
    public boolean actualizar(CambiosAlerta cambios, LocalDateTime ahora) {
        String anterior = status;
        if (cambios.alertType() != null) alertType = cambios.alertType();
        if (cambios.activationMethod() != null) activationMethod = cambios.activationMethod();
        if (cambios.status() != null) status = cambios.status();
        if (cambios.message() != null) message = cambios.message();
        if (cambios.endedAt() != null) endedAt = cambios.endedAt();
        if (cambios.cancelledAt() != null) cancelledAt = cambios.cancelledAt();
        if (!ACTIVA.equals(anterior) || !esEstadoFinal(status)) return false;
        marcarCierre(ahora);
        return true;
    }

    /**
     * Cambio de estado desde la administración: "resolved" guarda la hora de cierre y "cancelled" la
     * de cancelación. Devuelve true si terminó una alerta activa.
     */
    public boolean cambiarEstado(String estado, LocalDateTime ahora) {
        if (estado == null || !ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Estado no válido: " + estado);
        }
        String anterior = status;
        status = estado;
        marcarCierre(ahora);
        return ACTIVA.equals(anterior) && esEstadoFinal(estado);
    }

    /** Superó la duración máxima configurada. */
    public void expirar(LocalDateTime ahora) {
        status = "expired";
        endedAt = ahora;
    }

    /**
     * Marca (o desmarca) la alerta para conservar sus evidencias más allá de 60 días.
     * conservadas = cuántas alertas tiene ya marcadas la usuaria.
     */
    public void conservarEvidencias(boolean conservar, long kept) {
        if (conservar && !keepEvidence && kept >= MAX_ALERTAS_CONSERVADAS) {
            throw new IllegalArgumentException("Puedes conservar hasta " + MAX_ALERTAS_CONSERVADAS
                    + " alertas. Deja de conservar otra para guardar esta.");
        }
        keepEvidence = conservar;
    }

    private void marcarCierre(LocalDateTime ahora) {
        if ("resolved".equals(status) && endedAt == null) endedAt = ahora;
        if ("cancelled".equals(status) && cancelledAt == null) cancelledAt = ahora;
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
