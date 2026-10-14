package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LocationLog entity following database schema: Location tracking.
 * References user_profile_id and alert_id as foreign keys.
 */
@Entity
@Table(name = "location_log", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LocationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    private BigDecimal accuracy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();
}
