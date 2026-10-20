package com.alertamujer.settings.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Configuración global del sistema (fila única, singleton_key = 'SYSTEM').
 */
@Entity
@Table(name = "system_configuration", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SystemConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "singleton_key", nullable = false)
    private String singletonKey = "SYSTEM";

    @Column(name = "sos_max_duration_minutes", nullable = false)
    private Integer sosMaxDurationMinutes;

    @Column(name = "emergency_phone_number", nullable = false)
    private String emergencyPhoneNumber;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    @Column(name = "default_notification_priority", nullable = false)
    private String defaultNotificationPriority = "normal";

    @Column(name = "default_theme", nullable = false)
    private String defaultTheme = "light";

    @Column(name = "max_emergency_contacts", nullable = false)
    private Integer maxEmergencyContacts = 5;

    /** Versión mínima de la app móvil (p. ej. "1.2.0"); null si no se exige ninguna. */
    @Column(name = "min_app_version")
    private String minAppVersion;

    @Column(name = "updated_by_admin_id")
    private Integer updatedByAdminId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
