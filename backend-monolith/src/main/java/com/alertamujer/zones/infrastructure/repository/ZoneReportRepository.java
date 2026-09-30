package com.alertamujer.zones.infrastructure.repository;

import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneReportRepository extends JpaRepository<ZoneReport, Long> {
    List<ZoneReport> findByZoneId(Long zoneId);
    List<ZoneReport> findByUserProfileId(Long userProfileId);
    List<ZoneReport> findByStatus(String status);
}
