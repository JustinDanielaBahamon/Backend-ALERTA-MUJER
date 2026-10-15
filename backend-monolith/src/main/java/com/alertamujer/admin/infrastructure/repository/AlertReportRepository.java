package com.alertamujer.admin.infrastructure.repository;

import com.alertamujer.admin.domain.model.AlertReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertReportRepository extends JpaRepository<AlertReport, Long> {
}
