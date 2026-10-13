package com.alertamujer.devices.infrastructure.repository;

import com.alertamujer.devices.domain.model.DevicePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevicePermissionRepository extends JpaRepository<DevicePermission, Long> {
    List<DevicePermission> findByDeviceId(Long deviceId);
    Optional<DevicePermission> findByDeviceIdAndPermissionType(Long deviceId, String permissionType);
}
