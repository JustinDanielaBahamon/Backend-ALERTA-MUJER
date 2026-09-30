package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.AlertContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertContactRepository extends JpaRepository<AlertContact, Long> {
    List<AlertContact> findByAlertId(Long alertId);
}
