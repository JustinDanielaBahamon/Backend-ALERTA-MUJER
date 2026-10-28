package com.alertamujer.resources.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ResourceCallJpaRepository extends JpaRepository<ResourceCallJpaEntity, Long> {

    List<ResourceCallJpaEntity> findByUserProfileIdOrderByStartedAtDesc(Long userProfileId);

    List<ResourceCallJpaEntity> findByEmergencyResourceId(Long emergencyResourceId);

    List<ResourceCallJpaEntity> findByAlertId(Long alertId);
}
