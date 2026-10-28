package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Action of an admin on a moderation case (block, unblock).
 */
public class ModerationAction {

    private Long id;
    private Long userReportId;
    /** users.id of the admin. */
    private Integer adminProfileId;
    private String actionType;
    private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final String BLOCK = "block";
    public static final String UNBLOCK = "unblock";

    // ── Reglas ──────────────────────────────────────────────────────

    public static ModerationAction of(Long caseId, Long adminId, String type, String note) {
        ModerationAction a = new ModerationAction();
        a.userReportId = caseId;
        a.adminProfileId = adminId.intValue();
        a.actionType = type;
        a.notes = note;
        return a;
    }

    public boolean isBlock() {
        return BLOCK.equals(actionType);
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserReportId() { return userReportId; }
    public void setUserReportId(Long userReportId) { this.userReportId = userReportId; }
    public Integer getAdminProfileId() { return adminProfileId; }
    public void setAdminProfileId(Integer adminProfileId) { this.adminProfileId = adminProfileId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
