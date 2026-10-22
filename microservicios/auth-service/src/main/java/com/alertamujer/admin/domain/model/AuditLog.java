package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Registro de auditoría: cambios que guardan los triggers de la base y eventos de sesión de las
 * cuentas de administración.
 */
public class AuditLog {

    private Long id;
    private Long accountId;
    private String action;
    private String entityType;
    private Integer entityId;
    private String description;
    private String ipAddress;
    /** Desde dónde se hizo: "Panel web · Chrome · Linux", "App móvil · Android", "Tarea automática · …". */
    private String source;
    private LocalDateTime createdAt = LocalDateTime.now();

    public static final String TIPO_SESION = "SESSION";
    private static final int MAXIMO_DESCRIPCION = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    /** Evento de sesión de una administradora; el detalle se completa con el dispositivo. */
    public static AuditLog eventoDeSesion(Long accountId, String accion, String detalle, String device, String ip,
                                          String source) {
        AuditLog r = new AuditLog();
        r.accountId = accountId;
        r.action = accion;
        r.entityType = TIPO_SESION;
        String text = detalle == null ? device : device == null ? detalle : detalle + " · " + device;
        r.description = text == null ? null : text.length() > MAXIMO_DESCRIPCION ? text.substring(0, MAXIMO_DESCRIPCION) : text;
        r.ipAddress = ip;
        r.source = source;
        return r;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
