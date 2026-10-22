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

    public static final String DE_LA_DUENA = "owner";
    public static final String DE_CONTACTO = "contact";
    public static final int MAXIMO_LARGO = 500;
    public static final int MAXIMO_NOMBRE = 60;
    public static final int MAXIMO_POR_ALERTA = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    /**
     * Valida y crea un mensaje: solo mientras la alerta está activa, con texto y dentro de los
     * límites. enviados = mensajes que ya tiene la alerta.
     */
    public static AlertMessage escribir(Alert alert, String type, String name, String texto, long enviados) {
        if (!alert.estaActiva()) {
            throw new IllegalArgumentException("La alerta ya terminó: no se pueden enviar más mensajes");
        }
        String cuerpo = texto == null ? "" : texto.trim();
        if (cuerpo.isEmpty()) throw new IllegalArgumentException("El mensaje está vacío");
        if (cuerpo.length() > MAXIMO_LARGO) {
            throw new IllegalArgumentException("El mensaje puede tener como máximo " + MAXIMO_LARGO + " caracteres");
        }
        if (enviados >= MAXIMO_POR_ALERTA) {
            throw new IllegalArgumentException("Se alcanzó el máximo de mensajes de esta alerta");
        }
        AlertMessage m = new AlertMessage();
        m.alertId = alert.getId();
        m.senderType = type;
        m.senderName = nombreLimpio(name, DE_LA_DUENA.equals(type) ? "Ella" : "Contacto");
        m.body = cuerpo;
        return m;
    }

    private static String nombreLimpio(String name, String porDefecto) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) return porDefecto;
        return n.length() > MAXIMO_NOMBRE ? n.substring(0, MAXIMO_NOMBRE) : n;
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
