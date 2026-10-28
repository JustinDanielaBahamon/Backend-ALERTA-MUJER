package com.alertamujer.alerts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Row of the alerts table. Only the persistence adapter uses it; the domain is Alert. */
@Entity
@Table(name = "alert", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class AlertJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(name = "activation_method", nullable = false)
    private String activationMethod;

    @Column(name = "status", nullable = false)
    private String status;

    private String message;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "keep_evidence", nullable = false)
    private boolean keepEvidence;

    @Column(name = "extended_until")
    private LocalDateTime extendedUntil;

    @Column(name = "unanswered_at")
    private LocalDateTime unansweredAt;

    @Column(name = "duress_at")
    private LocalDateTime duressAt;

    // last_signal_at and signal_lost_at are written with targeted updates (see markSignal and the
    // notifications module): mapping them here would overwrite them with stale values on every save
}
