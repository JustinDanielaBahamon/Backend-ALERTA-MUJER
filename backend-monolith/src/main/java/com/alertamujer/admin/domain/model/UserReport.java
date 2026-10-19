package com.alertamujer.admin.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "user_report", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserReport {
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

    /** Gravedad para priorizar la moderación: low, medium, high o critical. */
    @Column(nullable = false)
    private String severity = "medium";

    private String status = "pending";

    @Column(name = "reviewed_by_admin_id")
    private Integer reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    /** Captura que respalda un bloqueo, como data URI de imagen (ver FotoDataUri). */
    @Column(columnDefinition = "text")
    private String evidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}