package com.alertamujer.admin.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de un caso de moderación. El dominio es UserReport. */
@Entity
@Table(name = "user_report", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class UserReportJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "reported_user_id", nullable = false)
    private Long reportedUserId;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    private String reason;

    private String description;

    @Column(nullable = false)
    private String severity;

    private String status;

    @Column(name = "reviewed_by_admin_id")
    private Integer reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(columnDefinition = "text")
    private String evidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
