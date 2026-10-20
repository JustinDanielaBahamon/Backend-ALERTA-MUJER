package com.alertamujer.devices.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de un dispositivo. El dominio es Device. */
@Entity
@Table(name = "device", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class DeviceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "device_uuid", nullable = false, unique = true)
    private String deviceUuid;

    private String brand;

    private String model;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "os_name", nullable = false)
    private String osName;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "gps_status", nullable = false)
    private String gpsStatus;

    private String status;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "fcm_token", length = 2000)
    private String fcmToken;

    @Column(length = 400)
    private String permissions;

    @Column(name = "permissions_updated_at")
    private LocalDateTime permissionsUpdatedAt;
}
