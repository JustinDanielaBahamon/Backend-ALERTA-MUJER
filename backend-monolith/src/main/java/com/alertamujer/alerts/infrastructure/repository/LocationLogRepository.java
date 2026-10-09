package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.LocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {
    List<LocationLog> findByUserProfileId(Long userProfileId);
    List<LocationLog> findByUserProfileIdOrderByRecordedAtDesc(Long userProfileId);
    List<LocationLog> findByAlertId(Long alertId);
}
