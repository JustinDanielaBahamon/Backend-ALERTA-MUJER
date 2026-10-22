package com.alertamujer.zones.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Row of a zone report. The domain is ZoneReport. */
@Entity
@Table(name = "zone_report", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ZoneReportJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(nullable = false)
    private String classification;

    private String comment;

    private String status;

    @Column(name = "reviewed_by_admin_id")
    private Integer reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
