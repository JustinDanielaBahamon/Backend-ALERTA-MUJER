package com.alertamujer.alerts.domain.model;

import java.time.LocalDateTime;

/**
 * Mensaje del chat de una alerta (de quien la activó o de uno de sus contactos).
 */
public class AlertMessage {

    private Long id;
    private Long alertId;
    /** "owner" (quien activó la alerta) o "contact". */
    private String senderType;
    private String senderName;
    private String body;
    private LocalDateTime createdAt = LocalDateTime.now();

    public static final String FROM_OWNER = "owner";
    public static final String FROM_CONTACT = "contact";
    public static final int MAX_LENGTH = 500;
    public static final int MAX_NAME = 60;
    public static final int MAX_PER_ALERT = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    /**
     * Valida y crea un mensaje: solo mientras la alerta está activa, con texto y dentro de los
     * límites. enviados = mensajes que ya tiene la alerta.
     */
    public static AlertMessage write(Alert alert, String type, String name, String text, long sentOnes) {
        if (!alert.isActive()) {
            throw new IllegalArgumentException("La alerta ya terminó: no se pueden enviar más mensajes");
        }
        String body = text == null ? "" : text.trim();
        if (body.isEmpty()) throw new IllegalArgumentException("El mensaje está vacío");
        if (body.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El mensaje puede tener como máximo " + MAX_LENGTH + " caracteres");
        }
        if (sentOnes >= MAX_PER_ALERT) {
            throw new IllegalArgumentException("Se alcanzó el máximo de mensajes de esta alerta");
        }
        AlertMessage m = new AlertMessage();
        m.alertId = alert.getId();
        m.senderType = type;
        m.senderName = cleanName(name, FROM_OWNER.equals(type) ? "Ella" : "Contacto");
        m.body = body;
        return m;
    }

    private static String cleanName(String name, String byDefault) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) return byDefault;
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME) : n;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
