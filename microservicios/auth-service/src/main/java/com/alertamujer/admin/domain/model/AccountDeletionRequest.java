package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Request to delete an account. Deleting is irreversible: one admin requests it and a different one
 * approves it.
 */
public class AccountDeletionRequest {

    private Long id;
    private Long userId;
    private Long requestedBy;
    private String reason;
    /** pending | approved | rejected */
    private String status = PENDING;
    private Long decidedBy;
    private String decisionNote;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime decidedAt;

    public static final String PENDING = "pending";
    private static final int MAX_NOTE = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    public static AccountDeletionRequest newOne(Long userId, Long adminId, String reason) {
        if (userId.equals(adminId)) throw new IllegalArgumentException("No puedes eliminar tu propia cuenta");
        AccountDeletionRequest s = new AccountDeletionRequest();
        s.userId = userId;
        s.requestedBy = adminId;
        s.reason = reason;
        return s;
    }

    /** Only an admin other than the requester can approve it. */
    public void approve(Long adminId, String note, LocalDateTime now) {
        verifyPending();
        if (requestedBy.equals(adminId)) throw new IllegalArgumentException("La aprobación debe hacerla otra administradora");
        decide("approved", adminId, note, now);
    }

    /** Reject or, if done by the requester, cancel the request. */
    public void reject(Long adminId, String note, LocalDateTime now) {
        decide("rejected", adminId, note, now);
    }

    public void verifyPending() {
        if (!PENDING.equals(status)) throw new IllegalArgumentException("Esta solicitud ya fue decidida");
    }

    private void decide(String newStatus, Long adminId, String note, LocalDateTime now) {
        verifyPending();
        status = newStatus;
        decidedBy = adminId;
        decisionNote = note == null || note.isBlank() ? null : ModerationText.truncate(note.trim(), MAX_NOTE);
        decidedAt = now;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDecidedBy() { return decidedBy; }
    public void setDecidedBy(Long decidedBy) { this.decidedBy = decidedBy; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}
