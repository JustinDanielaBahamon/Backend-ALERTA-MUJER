package com.alertamujer.alerts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de un mensaje del chat de una alerta. El dominio es AlertMessage. */
@Entity
@Table(name = "alert_message", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class AlertMessageJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "sender_type", nullable = false)
    private String senderType;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
