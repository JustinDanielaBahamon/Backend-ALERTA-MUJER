package com.alertamujer.devices.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, Long> {

    List<DeviceJpaEntity> findByAccountId(Long accountId);
}
