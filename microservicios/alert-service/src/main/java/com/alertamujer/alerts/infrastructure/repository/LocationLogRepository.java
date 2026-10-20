package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.LocationLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {
    List<LocationLog> findByUserProfileId(Long userProfileId);
    List<LocationLog> findByAlertId(Long alertId);
    List<LocationLog> findByUserProfileIdOrderByRecordedAtDesc(Long userProfileId, Pageable pageable);
    Optional<LocationLog> findFirstByUserProfileIdOrderByRecordedAtDesc(Long userProfileId);
    List<LocationLog> findByAlertIdOrderByRecordedAtAsc(Long alertId);

    /** Borra las ubicaciones del día a día (sin alerta) anteriores a la fecha. */
    @Modifying
    @Transactional
    @Query("delete from LocationLog l where l.alertId is null and l.recordedAt < :antes")
    int borrarSinAlertaAntesDe(@Param("antes") LocalDateTime antes);

    /** Borra los recorridos de las alertas anteriores a la fecha. */
    @Modifying
    @Transactional
    @Query("delete from LocationLog l where l.alertId is not null and l.recordedAt < :antes")
    int borrarDeAlertasAntesDe(@Param("antes") LocalDateTime antes);
}
