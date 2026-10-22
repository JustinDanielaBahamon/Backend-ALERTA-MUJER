package com.alertamujer.admin.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de una acción de moderación. El dominio es ModerationAction. */
@Entity
@Table(name = "moderation_action", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ModerationActionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_report_id", nullable = false)
    private Long userReportId;

    @Column(name = "admin_perfil_id", nullable = false)
    private Integer adminProfileId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
