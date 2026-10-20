package com.alertamujer.settings.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila única de configuración. El dominio es SystemConfiguration. */
@Entity
@Table(name = "system_configuration", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class SystemConfigurationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "singleton_key", nullable = false)
    private String singletonKey;

    @Column(name = "sos_max_duration_minutes", nullable = false)
    private Integer sosMaxDurationMinutes;

    @Column(name = "emergency_phone_number", nullable = false)
    private String emergencyPhoneNumber;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled;

    @Column(name = "default_notification_priority", nullable = false)
    private String defaultNotificationPriority;

    @Column(name = "default_theme", nullable = false)
    private String defaultTheme;

    @Column(name = "max_emergency_contacts", nullable = false)
    private Integer maxEmergencyContacts;

    @Column(name = "min_app_version")
    private String minAppVersion;

    @Column(name = "updated_by_admin_id")
    private Integer updatedByAdminId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
