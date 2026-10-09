package com.alertamujer.zones.application.service;

import com.alertamujer.zones.domain.model.ZoneReport;
import com.alertamujer.zones.infrastructure.repository.ZoneReportRepository;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ZoneReportService {

    private final ZoneReportRepository zoneReportRepository;
    private final UserProfileRepository userProfileRepository;

    public ZoneReportService(ZoneReportRepository zoneReportRepository, UserProfileRepository userProfileRepository) {
        this.zoneReportRepository = zoneReportRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<ZoneReport> getAllReports() {
        return zoneReportRepository.findAll();
    }

    public Optional<ZoneReport> getReportById(Long id) {
        return zoneReportRepository.findById(id);
    }

    public Optional<ZoneReport> getReportById(Long id, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        return zoneReportRepository.findById(id)
                .filter(report -> report.getUserProfileId().equals(userProfile.getId()));
    }

    public List<ZoneReport> getReportsByZone(Long zoneId) {
        return zoneReportRepository.findByZoneId(zoneId);
    }

    public List<ZoneReport> getReportsByUserProfile(Long userProfileId) {
        return zoneReportRepository.findByUserProfileId(userProfileId);
    }

    public List<ZoneReport> getReportsByUserId(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));
        return zoneReportRepository.findByUserProfileId(userProfile.getId());
    }

    public List<ZoneReport> getReportsByStatus(String status) {
        return zoneReportRepository.findByStatus(status);
    }

    public ZoneReport createReport(ZoneReport report, Long currentUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        if (report.getUserProfileId() != null && !report.getUserProfileId().equals(userProfile.getId())) {
            throw new AccessDeniedException("No puedes crear reportes para otro usuario");
        }
        
        report.setUserProfileId(userProfile.getId());
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
        
        UserProfile userProfile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", currentUserId));
        
        if (!report.getUserProfileId().equals(userProfile.getId())) {
            throw new AccessDeniedException("No puedes eliminar reportes de otro usuario");
        }
        
        zoneReportRepository.deleteById(id);
    }
}
