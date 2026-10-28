package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface EmergencyContactJpaRepository extends JpaRepository<EmergencyContactJpaEntity, Long> {

    List<EmergencyContactJpaEntity> findByUserProfileId(Long userProfileId);
}
