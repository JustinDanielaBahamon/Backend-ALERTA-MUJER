package com.alertamujer.alerts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Row of an alert delivery (contact + channel). The domain is AlertContact. */
@Entity
@Table(name = "alert_contact", schema = "alert",
        uniqueConstraints = @UniqueConstraint(columnNames = {"alert_id", "emergency_contact_id", "channel"}, name = "uk_alert_contact"))
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class AlertContactJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "emergency_contact_id")
    private Long emergencyContactId;

    @Column(nullable = false)
    private String channel;

    private String destination;

    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    private Integer attempts;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
