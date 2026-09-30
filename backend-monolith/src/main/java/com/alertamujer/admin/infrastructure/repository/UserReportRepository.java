package com.alertamujer.admin.infrastructure.repository;

import com.alertamujer.admin.domain.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByReportedUserId(Long reportedUserId);
    List<UserReport> findByReporterUserId(Long reporterUserId);
    List<UserReport> findByStatus(String status);
}
