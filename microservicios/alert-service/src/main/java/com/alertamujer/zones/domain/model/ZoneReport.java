package com.alertamujer.zones.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reporte de una usuaria sobre una zona (cómo la percibe); la administración lo aprueba o rechaza.
 */
public class ZoneReport {

    private Long id;
    private Long zoneId;
    private Long userProfileId;
    private String classification;
    private String comment;
    /** pending | approved | rejected */
    private String status = "pending";
    private Integer reviewedByAdminId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Reglas ──────────────────────────────────────────────────────

    /** La autora sale del token, nunca del body. */
    public static ZoneReport nuevo(Long perfil, Long zoneId, String classification, String comment) {
        ZoneReport r = new ZoneReport();
        r.userProfileId = perfil;
        r.zoneId = zoneId;
        r.classification = classification;
        r.comment = comment;
        return r;
    }

    public boolean esDe(Long perfil) {
        return perfil != null && Objects.equals(userProfileId, perfil);
    }

    public void aprobar(Integer adminId, LocalDateTime ahora) {
        revisar("approved", adminId, ahora);
    }

    public void rechazar(Integer adminId, LocalDateTime ahora) {
        revisar("rejected", adminId, ahora);
    }

    private void revisar(String estado, Integer adminId, LocalDateTime ahora) {
        status = estado;
        reviewedByAdminId = adminId;
        reviewedAt = ahora;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getReviewedByAdminId() { return reviewedByAdminId; }
    public void setReviewedByAdminId(Integer reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
