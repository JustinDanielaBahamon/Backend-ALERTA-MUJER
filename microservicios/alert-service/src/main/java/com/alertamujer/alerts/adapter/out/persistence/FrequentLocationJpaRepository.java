package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface FrequentLocationJpaRepository extends JpaRepository<FrequentLocationJpaEntity, Long> {

    List<FrequentLocationJpaEntity> findByUserProfileIdAndIsActiveTrue(Long userProfileId);
}
