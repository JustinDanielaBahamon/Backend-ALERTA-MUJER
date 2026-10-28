package com.alertamujer.resources.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Row of a call to a resource. The domain is ResourceCall. */
@Entity
@Table(name = "resource_call", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ResourceCallJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "emergency_resource_id", nullable = false)
    private Long emergencyResourceId;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "telephone_dialed", nullable = false)
    private String telephoneDialed;

    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
