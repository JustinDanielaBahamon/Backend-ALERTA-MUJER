package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Acción de una administradora sobre un caso de moderación (block, unblock).
 */
public class ModerationAction {

    private Long id;
    private Long userReportId;
    /** users.id de la administradora. */
    private Integer adminPerfilId;
    private String actionType;
    private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final String BLOQUEO = "block";
    public static final String DESBLOQUEO = "unblock";

    // ── Reglas ──────────────────────────────────────────────────────

    public static ModerationAction de(Long casoId, Long adminId, String type, String nota) {
        ModerationAction a = new ModerationAction();
        a.userReportId = casoId;
        a.adminPerfilId = adminId.intValue();
        a.actionType = type;
        a.notes = nota;
        return a;
    }

    public boolean esBloqueo() {
        return BLOQUEO.equals(actionType);
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserReportId() { return userReportId; }
    public void setUserReportId(Long userReportId) { this.userReportId = userReportId; }
    public Integer getAdminPerfilId() { return adminPerfilId; }
    public void setAdminPerfilId(Integer adminPerfilId) { this.adminPerfilId = adminPerfilId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
