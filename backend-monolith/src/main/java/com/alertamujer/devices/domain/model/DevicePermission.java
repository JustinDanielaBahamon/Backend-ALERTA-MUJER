package com.alertamujer.devices.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Estado de un permiso del sistema operativo (ubicación, micrófono, etc.) en un dispositivo.
 */
@Entity
@Table(name = "device_permission", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DevicePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "permission_type", nullable = false)
    private String permissionType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
