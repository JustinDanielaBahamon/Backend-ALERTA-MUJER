package com.alertamujer.alerts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fila del historial de ubicaciones. El dominio es LocationLog. */
@Entity
@Table(name = "location_log", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class LocationLogJpaEntity {
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
    private LocalDateTime recordedAt;
}
