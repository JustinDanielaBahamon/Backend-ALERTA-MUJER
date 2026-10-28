package com.alertamujer.settings.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SystemConfigurationJpaRepository extends JpaRepository<SystemConfigurationJpaEntity, Long> {

    Optional<SystemConfigurationJpaEntity> findBySingletonKey(String singletonKey);
}
