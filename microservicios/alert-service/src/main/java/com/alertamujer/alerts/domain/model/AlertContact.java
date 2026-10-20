package com.alertamujer.alerts.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Destinatario de una alerta: un contacto avisado por un canal (correo o push) y el estado de ese
 * envío.
 */
public class AlertContact {

    private Long id;
    private Long alertId;
    /** Null si la usuaria borró el contacto después de la alerta (el envío queda en el historial). */
    private Long emergencyContactId;
    private String channel;
    private String destination;
    private String status = "pending";
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private Integer attempts = 0;
    private String errorMessage;
    private LocalDateTime createdAt = LocalDateTime.now();

    public static final String CANAL_CORREO = "email";
    public static final String CANAL_PUSH = "push";

    private static final Set<String> ESTADOS_ABIERTOS = Set.of("pending", "sent");
    private static final int MAXIMO_ERROR = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    public static AlertContact nuevo(Long alertId, Long contactoId, String canal, String destino) {
        AlertContact d = new AlertContact();
        d.alertId = alertId;
        d.emergencyContactId = contactoId;
        d.channel = canal;
        d.destination = destino;
        return d;
    }

    /** Resultado de un intento de envío: "sent" o "failed" (con el motivo). */
    public void registrarIntento(String estado, String error, LocalDateTime ahora) {
        status = estado;
        attempts = (attempts == null ? 0 : attempts) + 1;
        if ("sent".equals(estado)) sentAt = ahora;
        errorMessage = error == null ? null : error.length() > MAXIMO_ERROR ? error.substring(0, MAXIMO_ERROR) : error;
    }

    /** Estado que ve la usuaria: un envío abierto de una alerta terminada se informa como "expired". */
    public String estadoVisible(boolean alertaTerminada) {
        return alertaTerminada && ESTADOS_ABIERTOS.contains(status) ? "expired" : status;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public Long getEmergencyContactId() { return emergencyContactId; }
    public void setEmergencyContactId(Long emergencyContactId) { this.emergencyContactId = emergencyContactId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
