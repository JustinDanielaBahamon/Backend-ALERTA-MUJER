package com.alertamujer.alerts.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Recipient of an alert: a contact notified through a channel (email or push) and the status of that
 * delivery.
 */
public class AlertContact {

    private Long id;
    private Long alertId;
    /** Null if the user deleted the contact after the alert (the delivery stays in the history). */
    private Long emergencyContactId;
    private String channel;
    private String destination;
    private String status = "pending";
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private Integer attempts = 0;
    private String errorMessage;
    private LocalDateTime createdAt = LocalDateTime.now();

    public static final String EMAIL_CHANNEL = "email";
    public static final String PUSH_CHANNEL = "push";

    private static final Set<String> OPEN_STATUSES = Set.of("pending", "sent");
    private static final int MAX_ERROR = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    public static AlertContact created(Long alertId, Long contactId, String channel, String target) {
        AlertContact d = new AlertContact();
        d.alertId = alertId;
        d.emergencyContactId = contactId;
        d.channel = channel;
        d.destination = target;
        return d;
    }

    /** Result of a delivery attempt: "sent" or "failed" (with the reason). */
    public void recordAttempt(String newStatus, String error, LocalDateTime now) {
        status = newStatus;
        attempts = (attempts == null ? 0 : attempts) + 1;
        if ("sent".equals(newStatus)) sentAt = now;
        errorMessage = error == null ? null : error.length() > MAX_ERROR ? error.substring(0, MAX_ERROR) : error;
    }

    /** Status the user sees: an open delivery of an ended alert is reported as "expired". */
    public String visibleStatus(boolean alertEnded) {
        return alertEnded && OPEN_STATUSES.contains(status) ? "expired" : status;
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
