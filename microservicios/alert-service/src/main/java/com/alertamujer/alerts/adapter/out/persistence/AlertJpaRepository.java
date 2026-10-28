package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, Long> {

    List<AlertJpaEntity> findByUserProfileId(Long userProfileId);

    List<AlertJpaEntity> findByStatus(String status);

    Optional<AlertJpaEntity> findFirstByUserProfileIdAndStatusOrderByStartedAtDesc(Long userProfileId, String status);

    long countByUserProfileIdAndKeepEvidenceTrue(Long userProfileId);

    /** Only this column: a whole save could undo a status change made at the same moment. */
    @Transactional
    @Modifying
    @Query(value = "UPDATE alert.alert SET last_signal_at = :when WHERE id = :id AND status = 'active'", nativeQuery = true)
    int markSignal(@Param("id") Long id, @Param("when") LocalDateTime when);
}
