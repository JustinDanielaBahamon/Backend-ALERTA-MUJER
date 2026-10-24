package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface SafeTripJpaRepository extends JpaRepository<SafeTripJpaEntity, Long> {

    Optional<SafeTripJpaEntity> findFirstByUserProfileIdAndStatus(Long userProfileId, String status);

    List<SafeTripJpaEntity> findByStatus(String status);

    long countByUserProfileIdAndCreatedAtAfter(Long userProfileId, LocalDateTime since);
}
