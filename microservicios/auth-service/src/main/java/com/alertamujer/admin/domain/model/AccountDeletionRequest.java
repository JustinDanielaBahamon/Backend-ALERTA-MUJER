package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;

/**
 * Solicitud para eliminar una cuenta. Eliminar es irreversible: la pide una administradora y la
 * aprueba otra distinta.
 */
public class AccountDeletionRequest {

    private Long id;
    private Long userId;
    private Long requestedBy;
    private String reason;
    /** pending | approved | rejected */
    private String status = PENDIENTE;
    private Long decidedBy;
    private String decisionNote;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime decidedAt;

    public static final String PENDIENTE = "pending";
    private static final int MAXIMO_NOTA = 500;

    // ── Reglas ──────────────────────────────────────────────────────

    public static AccountDeletionRequest nueva(Long userId, Long adminId, String motivo) {
        if (userId.equals(adminId)) throw new IllegalArgumentException("No puedes eliminar tu propia cuenta");
        AccountDeletionRequest s = new AccountDeletionRequest();
        s.userId = userId;
        s.requestedBy = adminId;
        s.reason = motivo;
        return s;
    }

    /** Solo la aprueba una administradora distinta de la que la pidió. */
    public void aprobar(Long adminId, String nota, LocalDateTime ahora) {
        verificarPendiente();
        if (requestedBy.equals(adminId)) throw new IllegalArgumentException("La aprobación debe hacerla otra administradora");
        decidir("approved", adminId, nota, ahora);
    }

    /** Rechazar o, si es quien la pidió, cancelar la solicitud. */
    public void rechazar(Long adminId, String nota, LocalDateTime ahora) {
        decidir("rejected", adminId, nota, ahora);
    }

    public void verificarPendiente() {
        if (!PENDIENTE.equals(status)) throw new IllegalArgumentException("Esta solicitud ya fue decidida");
    }

    private void decidir(String newStatus, Long adminId, String nota, LocalDateTime ahora) {
        verificarPendiente();
        status = newStatus;
        decidedBy = adminId;
        decisionNote = nota == null || nota.isBlank() ? null : TextoModeracion.recortar(nota.trim(), MAXIMO_NOTA);
        decidedAt = ahora;
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
