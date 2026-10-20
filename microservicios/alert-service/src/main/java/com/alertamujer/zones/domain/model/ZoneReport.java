package com.alertamujer.zones.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "zone_report", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ZoneReport {
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
    private String status = "pending";

    @Column(name = "reviewed_by_admin_id")
    private Integer reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}