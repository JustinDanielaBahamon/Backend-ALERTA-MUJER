package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

interface ProtectionDeviceJpaRepository extends JpaRepository<ProtectionDeviceJpaEntity, Long> {

    Optional<ProtectionDeviceJpaEntity> findByKeyHash(String keyHash);

    boolean existsByUserProfileIdAndDeviceId(Long userProfileId, Long deviceId);

    @Transactional
    @Modifying
    void deleteByUserProfileIdAndDeviceId(Long userProfileId, Long deviceId);
}
