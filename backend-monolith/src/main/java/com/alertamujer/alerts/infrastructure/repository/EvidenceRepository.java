package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByAlertId(Long alertId);
}
