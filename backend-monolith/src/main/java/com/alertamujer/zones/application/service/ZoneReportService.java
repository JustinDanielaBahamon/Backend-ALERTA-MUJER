package com.alertamujer.zones.application.service;

import com.alertamujer.zones.domain.model.ZoneReport;
import com.alertamujer.zones.infrastructure.repository.ZoneReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ZoneReportService {

    private final ZoneReportRepository zoneReportRepository;

    public ZoneReportService(ZoneReportRepository zoneReportRepository) {
        this.zoneReportRepository = zoneReportRepository;
    }

    public List<ZoneReport> getAllReports() {
        return zoneReportRepository.findAll();
    }

    public Optional<ZoneReport> getReportById(Long id) {
        return zoneReportRepository.findById(id);
    }

    public List<ZoneReport> getReportsByZone(Long zoneId) {
        return zoneReportRepository.findByZoneId(zoneId);
    }

    public List<ZoneReport> getReportsByUserProfile(Long userProfileId) {
        return zoneReportRepository.findByUserProfileId(userProfileId);
    }

    public List<ZoneReport> getReportsByStatus(String status) {
        return zoneReportRepository.findByStatus(status);
    }

    public ZoneReport createReport(ZoneReport report) {
        return zoneReportRepository.save(report);
    }

    public ZoneReport approveReport(Long id, Integer adminId) {
        ZoneReport report = zoneReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone report not found"));
        
        report.setStatus("approved");
        report.setReviewedByAdminId(adminId);
        report.setReviewedAt(LocalDateTime.now());
        
        return zoneReportRepository.save(report);
    }

    public ZoneReport rejectReport(Long id, Integer adminId) {
        ZoneReport report = zoneReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone report not found"));
        
        report.setStatus("rejected");
        report.setReviewedByAdminId(adminId);
        report.setReviewedAt(LocalDateTime.now());
        
        return zoneReportRepository.save(report);
    }

    public void deleteReport(Long id) {
        zoneReportRepository.deleteById(id);
    }
}
