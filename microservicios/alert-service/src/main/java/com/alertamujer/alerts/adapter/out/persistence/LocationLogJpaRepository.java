package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface LocationLogJpaRepository extends JpaRepository<LocationLogJpaEntity, Long> {

    List<LocationLogJpaEntity> findByUserProfileIdOrderByRecordedAtDesc(Long userProfileId, Pageable pageable);

    Optional<LocationLogJpaEntity> findFirstByUserProfileIdOrderByRecordedAtDesc(Long userProfileId);

    List<LocationLogJpaEntity> findByAlertIdOrderByRecordedAtAsc(Long alertId);

    @Modifying
    @Transactional
    @Query("delete from LocationLogJpaEntity l where l.alertId is null and l.recordedAt < :antes")
    int deleteWithoutAlertBefore(@Param("antes") LocalDateTime before);

    @Modifying
    @Transactional
    @Query("delete from LocationLogJpaEntity l where l.alertId is not null and l.recordedAt < :antes")
    int deleteFromAlertsBefore(@Param("antes") LocalDateTime before);
}
