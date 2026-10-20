package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, Long> {

    List<AlertJpaEntity> findByUserProfileId(Long userProfileId);

    List<AlertJpaEntity> findByStatus(String status);

    Optional<AlertJpaEntity> findFirstByUserProfileIdAndStatusOrderByStartedAtDesc(Long userProfileId, String status);

    long countByUserProfileIdAndKeepEvidenceTrue(Long userProfileId);
}
