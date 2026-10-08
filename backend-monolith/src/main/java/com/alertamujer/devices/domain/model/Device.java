package com.alertamujer.devices.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Device entity following database schema: Device information.
 * Belongs to Account via account_id foreign key.
 */
@Entity
@Table(name = "device", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Device {
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

    @Column(name = "os_name", nullable = false)
    private String osName;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "gps_status", nullable = false)
    private String gpsStatus = "unknown";

    private String status = "active";

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
