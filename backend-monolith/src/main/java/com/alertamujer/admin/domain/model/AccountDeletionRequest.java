package com.alertamujer.admin.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Solicitud para eliminar una cuenta: la pide una administradora y la aprueba otra distinta. */
@Entity
@Table(name = "account_deletion_request", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccountDeletionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "decided_by")
    private Long decidedBy;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
