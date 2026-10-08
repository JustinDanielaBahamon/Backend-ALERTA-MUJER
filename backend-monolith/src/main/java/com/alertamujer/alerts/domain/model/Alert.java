package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Alert entity following database schema: Alert state and type.
 * References user_profile_id and device_id as foreign keys.
 */
@Entity
@Table(name = "alert", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "alert_type", nullable = false)
    private String alertType = "main";

    @Column(name = "activation_method", nullable = false)
    private String activationMethod;

    @Column(name = "status", nullable = false)
    private String status = "active";

    private String message;

    @Column(name = "latitude", precision = 9, scale = 6)
    private java.math.BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private java.math.BigDecimal longitude;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
