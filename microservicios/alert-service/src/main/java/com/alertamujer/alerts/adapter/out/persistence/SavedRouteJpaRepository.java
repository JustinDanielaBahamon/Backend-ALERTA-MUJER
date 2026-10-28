package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SavedRouteJpaRepository extends JpaRepository<SavedRouteJpaEntity, Long> {

    List<SavedRouteJpaEntity> findByUserProfileIdOrderByCreatedAtDesc(Long userProfileId);

    long countByUserProfileId(Long userProfileId);

    long countByUserProfileIdAndIsStarredTrue(Long userProfileId);
}
