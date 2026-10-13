package com.alertamujer.devices.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Métodos de activación de alerta habilitados por dispositivo (botón, gesto, voz...).
 */
@Entity
@Table(name = "alert_activation_setting", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AlertActivationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "activation_method", nullable = false)
    private String activationMethod;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
