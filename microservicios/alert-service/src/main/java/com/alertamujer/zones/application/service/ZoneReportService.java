package com.alertamujer.zones.application.service;

import com.alertamujer.zones.domain.model.ZoneReport;
import com.alertamujer.zones.infrastructure.repository.ZoneReportRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
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

    public Optional<ZoneReport> getReportById(Long id, Long currentUserId) {
        return zoneReportRepository.findById(id)
                .filter(report -> report.getUserProfileId().equals(currentUserId));
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

    public ZoneReport createReport(ZoneReport report, Long currentUserId) {
        if (!report.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes crear reportes para otro usuario");
        }
        return zoneReportRepository.save(report);
    }

    public ZoneReport approveReport(Long id, Integer adminId) {
        ZoneReport report = zoneReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
        
        report.setStatus("approved");
        report.setReviewedByAdminId(adminId);
        report.setReviewedAt(LocalDateTime.now());
        
        return zoneReportRepository.save(report);
    }

    public ZoneReport rejectReport(Long id, Integer adminId) {
        ZoneReport report = zoneReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
        
        report.setStatus("rejected");
        report.setReviewedByAdminId(adminId);
        report.setReviewedAt(LocalDateTime.now());
        
        return zoneReportRepository.save(report);
    }

    public void deleteReport(Long id, Long currentUserId) {
        ZoneReport report = zoneReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
        
        if (!report.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes eliminar reportes de otro usuario");
        }
        
        zoneReportRepository.deleteById(id);
    }
}
