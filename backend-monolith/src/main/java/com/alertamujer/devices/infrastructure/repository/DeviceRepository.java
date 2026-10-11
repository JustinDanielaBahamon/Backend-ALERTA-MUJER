package com.alertamujer.devices.infrastructure.repository;

import com.alertamujer.devices.domain.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByAccountId(Long accountId);
}
