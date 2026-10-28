package com.alertamujer.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DeviceSessionJpaRepository extends JpaRepository<DeviceSessionJpaEntity, Long> {

    List<DeviceSessionJpaEntity> findByAccountIdAndStatus(Long accountId, String status);
}
