package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alert report saved by the administration: a name, descriptive data and the alerts it includes
 * (chosen by hand or by filters). Exported to PDF or Excel.
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
    /** Comma-separated alert ids; empty = use the filters. */
    private String alertIds;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    /** Optional filter by alert status. */
    private String alertStatus;
    private Integer exportCount = 0;
    private Long createdByUserId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final Set<String> STATUSES = Set.of("pending", "attended", "closed");
    public static final Set<String> FORMATS = Set.of("PDF", "EXCEL");
    public static final Set<String> ALERT_STATUSES = Set.of("active", "resolved", "cancelled", "expired");

    // ── Reglas ──────────────────────────────────────────────────────

    public static AlertReport created(ReportExportChanges changes, Long adminUserId) {
        if (changes.name() == null || changes.name().isBlank()) {
            throw new IllegalArgumentException("El nombre del informe es obligatorio");
        }
        AlertReport r = new AlertReport();
        r.apply(changes);
        r.createdByUserId = adminUserId;
        return r;
    }

    /** Applies the sent fields validating status, format and filter; "" in city or filter removes them. */
    public void apply(ReportExportChanges c) {
        if (c.name() != null && !c.name().isBlank()) name = c.name().trim();
        if (c.city() != null) city = c.city().isBlank() ? null : c.city().trim();
        if (c.status() != null) {
            if (!STATUSES.contains(c.status())) throw new IllegalArgumentException("Estado no válido: " + c.status());
            status = c.status();
        }
        if (c.format() != null) {
            if (!FORMATS.contains(c.format())) throw new IllegalArgumentException("Formato no válido: " + c.format());
            format = c.format();
        }
        if (c.userLabel() != null) userLabel = c.userLabel().trim();
        if (c.event() != null) event = c.event().trim();
        if (c.description() != null) description = c.description().trim();
        if (c.alertIds() != null) alertIds = c.alertIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        if (c.fromDate() != null) fromDate = c.fromDate();
        if (c.toDate() != null) toDate = c.toDate();
        if (c.alertStatus() != null) {
            if (!c.alertStatus().isBlank() && !ALERT_STATUSES.contains(c.alertStatus())) {
                throw new IllegalArgumentException("Estado de alerta no válido: " + c.alertStatus());
            }
            alertStatus = c.alertStatus().isBlank() ? null : c.alertStatus();
        }
    }

    public Set<Long> chosenIds() {
        if (alertIds == null || alertIds.isBlank()) return Set.of();
        return Arrays.stream(alertIds.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toSet());
    }

    /** Whether the alert goes in the report: if alerts were chosen, only those; otherwise those matching the filters. */
    public boolean includes(Long alertId, LocalDateTime start, String status) {
        Set<Long> ids = chosenIds();
        if (!ids.isEmpty()) return ids.contains(alertId);
        return (fromDate == null || (start != null && !start.isBefore(fromDate)))
                && (toDate == null || (start != null && !start.isAfter(toDate)))
                && (alertStatus == null || alertStatus.equals(status));
    }

    public void recordExport() {
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
