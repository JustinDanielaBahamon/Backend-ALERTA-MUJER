package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Informe de alertas guardado por la administración: un nombre, datos descriptivos y las alertas que
 * incluye (elegidas a mano o por filtros). Se exporta a PDF o Excel.
 */
public class AlertReport {

    private Long id;
    private String name;
    private String city;
    /** pending, attended o closed. */
    private String status = "pending";
    private String userLabel;
    private String event;
    private String description;
    /** PDF o EXCEL: formato preferido al exportar. */
    private String format = "PDF";
    /** Ids de alertas separados por coma; vacío = usar los filtros. */
    private String alertIds;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    /** Filtro opcional por estado de la alerta. */
    private String alertStatus;
    private Integer exportCount = 0;
    private Long createdByUserId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final Set<String> ESTADOS = Set.of("pending", "attended", "closed");
    public static final Set<String> FORMATOS = Set.of("PDF", "EXCEL");
    public static final Set<String> ESTADOS_ALERTA = Set.of("active", "resolved", "cancelled", "expired");

    // ── Reglas ──────────────────────────────────────────────────────

    public static AlertReport nuevo(CambiosInforme cambios, Long adminUserId) {
        if (cambios.name() == null || cambios.name().isBlank()) {
            throw new IllegalArgumentException("El nombre del informe es obligatorio");
        }
        AlertReport r = new AlertReport();
        r.aplicar(cambios);
        r.createdByUserId = adminUserId;
        return r;
    }

    /** Aplica los campos enviados validando estado, formato y filtro; "" en ciudad o filtro los quita. */
    public void aplicar(CambiosInforme c) {
        if (c.name() != null && !c.name().isBlank()) name = c.name().trim();
        if (c.city() != null) city = c.city().isBlank() ? null : c.city().trim();
        if (c.status() != null) {
            if (!ESTADOS.contains(c.status())) throw new IllegalArgumentException("Estado no válido: " + c.status());
            status = c.status();
        }
        if (c.format() != null) {
            if (!FORMATOS.contains(c.format())) throw new IllegalArgumentException("Formato no válido: " + c.format());
            format = c.format();
        }
        if (c.userLabel() != null) userLabel = c.userLabel().trim();
        if (c.event() != null) event = c.event().trim();
        if (c.description() != null) description = c.description().trim();
        if (c.alertIds() != null) alertIds = c.alertIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        if (c.fromDate() != null) fromDate = c.fromDate();
        if (c.toDate() != null) toDate = c.toDate();
        if (c.alertStatus() != null) {
            if (!c.alertStatus().isBlank() && !ESTADOS_ALERTA.contains(c.alertStatus())) {
                throw new IllegalArgumentException("Estado de alerta no válido: " + c.alertStatus());
            }
            alertStatus = c.alertStatus().isBlank() ? null : c.alertStatus();
        }
    }

    public Set<Long> idsElegidos() {
        if (alertIds == null || alertIds.isBlank()) return Set.of();
        return Arrays.stream(alertIds.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toSet());
    }

    /** La alerta entra en el informe: si hay alertas elegidas, solo esas; si no, las que cumplen los filtros. */
    public boolean incluye(Long alertaId, LocalDateTime inicio, String estadoAlerta) {
        Set<Long> ids = idsElegidos();
        if (!ids.isEmpty()) return ids.contains(alertaId);
        return (fromDate == null || (inicio != null && !inicio.isBefore(fromDate)))
                && (toDate == null || (inicio != null && !inicio.isAfter(toDate)))
                && (alertStatus == null || alertStatus.equals(estadoAlerta));
    }

    public void registrarExportacion() {
        exportCount = (exportCount == null ? 0 : exportCount) + 1;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUserLabel() { return userLabel; }
    public void setUserLabel(String userLabel) { this.userLabel = userLabel; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getAlertIds() { return alertIds; }
    public void setAlertIds(String alertIds) { this.alertIds = alertIds; }
    public LocalDateTime getFromDate() { return fromDate; }
    public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
    public LocalDateTime getToDate() { return toDate; }
    public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) { this.alertStatus = alertStatus; }
    public Integer getExportCount() { return exportCount; }
    public void setExportCount(Integer exportCount) { this.exportCount = exportCount; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
