package com.alertamujer.admin.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Acción de moderación tomada por un admin sobre un reporte de usuario.
 */
@Entity
@Table(name = "moderation_action", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ModerationAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_report_id", nullable = false)
    private Long userReportId;

    @Column(name = "admin_perfil_id", nullable = false)
    private Integer adminPerfilId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
