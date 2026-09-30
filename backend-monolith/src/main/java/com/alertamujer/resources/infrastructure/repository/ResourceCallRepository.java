package com.alertamujer.resources.infrastructure.repository;

import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceCallRepository extends JpaRepository<ResourceCall, Long> {
    List<ResourceCall> findByUserProfileId(Long userProfileId);
    List<ResourceCall> findByEmergencyResourceId(Long emergencyResourceId);
    List<ResourceCall> findByAlertId(Long alertId);
}
