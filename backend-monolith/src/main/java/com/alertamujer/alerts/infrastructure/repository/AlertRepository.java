package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserProfileId(Long userProfileId);
    List<Alert> findByStatus(String status);
    Optional<Alert> findFirstByUserProfileIdAndStatusOrderByStartedAtDesc(Long userProfileId, String status);

    /** Alertas de la usuaria marcadas para conservar sus evidencias. */
    long countByUserProfileIdAndKeepEvidenceTrue(Long userProfileId);
}
