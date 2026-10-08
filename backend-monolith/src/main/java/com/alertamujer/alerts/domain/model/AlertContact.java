package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AlertContact entity following database schema: Alert-contact link table.
 * References alert_id and emergency_contact_id as foreign keys.
 */
@Entity
@Table(name = "alert_contact", schema = "alert",
    uniqueConstraints = @UniqueConstraint(columnNames = {"alert_id", "emergency_contact_id", "channel"}, name = "uk_alert_contact"))
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AlertContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "emergency_contact_id", nullable = false)
    private Long emergencyContactId;

    @Column(nullable = false)
    private String channel;

    private String destination;
    private String status = "pending";

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    private Integer attempts = 0;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
