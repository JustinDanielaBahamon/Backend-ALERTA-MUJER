package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Mensaje del chat de una alerta (de quien la activó o de uno de sus contactos). */
@Entity
@Table(name = "alert_message", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AlertMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    /** "owner" (quien activó la alerta) o "contact". */
    @Column(name = "sender_type", nullable = false)
    private String senderType;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
