package com.alertamujer.zones.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A user's report about a zone (how she perceives it); the administration approves or rejects it.
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

    /** The author comes from the token, never from the body. */
    public static ZoneReport created(Long profile, Long zoneId, String classification, String comment) {
        ZoneReport r = new ZoneReport();
        r.userProfileId = profile;
        r.zoneId = zoneId;
        r.classification = classification;
        r.comment = comment;
        return r;
    }

    public boolean esDe(Long profile) {
        return profile != null && Objects.equals(userProfileId, profile);
    }

    public void approve(Integer adminId, LocalDateTime now) {
        review("approved", adminId, now);
    }

    public void reject(Integer adminId, LocalDateTime now) {
        review("rejected", adminId, now);
    }

    private void review(String newStatus, Integer adminId, LocalDateTime now) {
        status = newStatus;
        reviewedByAdminId = adminId;
        reviewedAt = now;
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
