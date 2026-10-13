package com.alertamujer.admin.infrastructure.repository;

import com.alertamujer.admin.domain.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    Optional<SystemConfiguration> findBySingletonKey(String singletonKey);
}
