package com.alertamujer.devices.infrastructure.repository;

import com.alertamujer.devices.domain.model.AlertActivationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertActivationSettingRepository extends JpaRepository<AlertActivationSetting, Long> {
    List<AlertActivationSetting> findByDeviceId(Long deviceId);
    Optional<AlertActivationSetting> findByDeviceIdAndActivationMethod(Long deviceId, String activationMethod);
}
