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

    public static final Set<String> ESTADOS = Set.of("pending", "in_review", "reviewed", "resolved", "approved", "rejected");
    public static final Set<String> CERRADOS = Set.of("reviewed", "resolved", "approved", "rejected");
    public static final Set<String> GRAVEDADES = Set.of("low", "medium", "high", "critical");

    // ── Reglas ──────────────────────────────────────────────────────

    /** Reporte que crea la administración sobre una usuaria. */
    public static UserReport reporte(Long reportadaId, Long adminId, String motivo, String descripcion, String gravedad) {
        if (motivo == null || motivo.isBlank() || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("El motivo y la descripción son obligatorios");
        }
        UserReport r = new UserReport();
        r.reportedUserId = reportadaId;
        r.reporterUserId = adminId;
        r.reason = motivo.trim();
        r.description = descripcion.trim();
        r.severity = validarGravedad(gravedad == null ? "medium" : gravedad);
        return r;
    }

    /** Caso cerrado que deja un bloqueo hecho desde Gestión de usuarios. */
    public static UserReport bloqueo(Long userId, Long adminId, String motivo, String detalle, String evidence, LocalDateTime ahora) {
        UserReport r = cerrado(userId, adminId, motivo, detalle, ahora);
        r.severity = "high";
        r.resolutionNotes = "Cuenta bloqueada desde Gestión de usuarios";
        r.evidence = evidence;
        return r;
    }

    /** Caso para anotar un desbloqueo cuando la usuaria no tiene ninguno (bloqueo anterior a este registro). */
    public static UserReport desbloqueoSinCaso(Long userId, Long adminId, String detalle, LocalDateTime ahora) {
        return cerrado(userId, adminId, "Desbloqueo", detalle, ahora);
    }

    public static String validarGravedad(String gravedad) {
        if (!GRAVEDADES.contains(gravedad)) throw new IllegalArgumentException("Gravedad no válida: " + gravedad);
        return gravedad;
    }

    /** Cambia los campos enviados; al cerrar el caso guarda quién y cuándo lo revisó. */
    public void actualizar(CambiosReporte c, Long adminId, LocalDateTime ahora) {
        if (c.reason() != null && !c.reason().isBlank()) reason = c.reason().trim();
        if (c.description() != null && !c.description().isBlank()) description = c.description().trim();
        if (c.severity() != null) severity = validarGravedad(c.severity());
        if (c.resolutionNotes() != null) resolutionNotes = c.resolutionNotes().trim();
        if (c.status() != null) {
            if (!ESTADOS.contains(c.status())) throw new IllegalArgumentException("Estado no válido: " + c.status());
            status = c.status();
            if (CERRADOS.contains(c.status())) {
                reviewedAt = ahora;
                reviewedByAdminId = adminId == null ? null : adminId.intValue();
            }
        }
        updatedAt = ahora;
    }

    public boolean tieneEvidencia() {
        return evidence != null;
    }

    private static UserReport cerrado(Long userId, Long adminId, String motivo, String detalle, LocalDateTime ahora) {
        UserReport r = new UserReport();
        r.reportedUserId = userId;
        r.reporterUserId = adminId;
        r.reason = motivo;
        r.description = detalle;
        r.status = "resolved";
        r.reviewedByAdminId = adminId.intValue();
        r.reviewedAt = ahora;
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
