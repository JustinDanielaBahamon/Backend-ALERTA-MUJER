package com.alertamujer.resources.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ResourceCall entity following database schema: Emergency resource call records.
 * References emergency_resource_id, user_profile_id, alert_id, device_id as foreign keys.
 */
@Entity
@Table(name = "resource_call", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResourceCall {
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

    private String status = "completed";

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
