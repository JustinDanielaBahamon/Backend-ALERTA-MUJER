package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Caso de moderación sobre una usuaria: un reporte de la administración o el registro de un bloqueo
 * (con su motivo y, si hay, una captura). Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class UserReport {

    private Long id;
    private Long reportedUserId;
    private Long reporterUserId;
    private String reason;
    private String description;
    /** Gravedad para priorizar la moderación: low, medium, high o critical. */
    private String severity = "medium";
    private String status = "pending";
    private Integer reviewedByAdminId;
    private LocalDateTime reviewedAt;
    private String resolutionNotes;
    /** Captura que respalda un bloqueo, como data URI de imagen. */
    private String evidence;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final Set<String> STATUSES = Set.of("pending", "in_review", "reviewed", "resolved", "approved", "rejected");
    public static final Set<String> CLOSED = Set.of("reviewed", "resolved", "approved", "rejected");
    public static final Set<String> SEVERITIES = Set.of("low", "medium", "high", "critical");

    // ── Reglas ──────────────────────────────────────────────────────

    /** Reporte que crea la administración sobre una usuaria. */
    public static UserReport userReport(Long reportedId, Long adminId, String reason, String description, String severity) {
        if (reason == null || reason.isBlank() || description == null || description.isBlank()) {
            throw new IllegalArgumentException("El motivo y la descripción son obligatorios");
        }
        UserReport r = new UserReport();
        r.reportedUserId = reportedId;
        r.reporterUserId = adminId;
        r.reason = reason.trim();
        r.description = description.trim();
        r.severity = validateSeverity(severity == null ? "medium" : severity);
        return r;
    }

    /** Caso cerrado que deja un bloqueo hecho desde Gestión de usuarios. */
    public static UserReport block(Long userId, Long adminId, String reason, String detail, String evidence, LocalDateTime now) {
        UserReport r = closed(userId, adminId, reason, detail, now);
        r.severity = "high";
        r.resolutionNotes = "Cuenta bloqueada desde Gestión de usuarios";
        r.evidence = evidence;
        return r;
    }

    /** Caso para anotar un desbloqueo cuando la usuaria no tiene ninguno (bloqueo anterior a este registro). */
    public static UserReport unblockWithoutCase(Long userId, Long adminId, String detail, LocalDateTime now) {
        return closed(userId, adminId, "Desbloqueo", detail, now);
    }

    public static String validateSeverity(String severity) {
        if (!SEVERITIES.contains(severity)) throw new IllegalArgumentException("Gravedad no válida: " + severity);
        return severity;
    }

    /** Cambia los campos enviados; al cerrar el caso guarda quién y cuándo lo revisó. */
    public void update(ReportChanges c, Long adminId, LocalDateTime now) {
        if (c.reason() != null && !c.reason().isBlank()) reason = c.reason().trim();
        if (c.description() != null && !c.description().isBlank()) description = c.description().trim();
        if (c.severity() != null) severity = validateSeverity(c.severity());
        if (c.resolutionNotes() != null) resolutionNotes = c.resolutionNotes().trim();
        if (c.status() != null) {
            if (!STATUSES.contains(c.status())) throw new IllegalArgumentException("Estado no válido: " + c.status());
            status = c.status();
            if (CLOSED.contains(c.status())) {
                reviewedAt = now;
                reviewedByAdminId = adminId == null ? null : adminId.intValue();
            }
        }
        updatedAt = now;
    }

    public boolean hasEvidence() {
        return evidence != null;
    }

    private static UserReport closed(Long userId, Long adminId, String reason, String detail, LocalDateTime now) {
        UserReport r = new UserReport();
        r.reportedUserId = userId;
        r.reporterUserId = adminId;
        r.reason = reason;
        r.description = detail;
        r.status = "resolved";
        r.reviewedByAdminId = adminId.intValue();
        r.reviewedAt = now;
        return r;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }
    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getReviewedByAdminId() { return reviewedByAdminId; }
    public void setReviewedByAdminId(Integer reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
