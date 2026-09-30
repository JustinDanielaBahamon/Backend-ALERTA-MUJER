package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserProfileId(Long userProfileId);
    List<Alert> findByStatus(String status);
}
