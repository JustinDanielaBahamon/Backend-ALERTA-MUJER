package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.admin.infrastructure.controller.dto.AdminUserDto;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.admin.infrastructure.repository.UserReportRepository;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.zones.infrastructure.repository.ZoneRepository;
import com.alertamujer.zones.infrastructure.repository.ZoneReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final AlertRepository alertRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final EvidenceRepository evidenceRepository;
    private final DeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;
    private final ZoneReportRepository zoneReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserReportRepository userReportRepository;

    public AdminService(UserRepository userRepository, AccountRepository accountRepository,
                        UserProfileRepository userProfileRepository, AlertRepository alertRepository,
                        EmergencyContactRepository emergencyContactRepository, EvidenceRepository evidenceRepository,
                        DeviceRepository deviceRepository, ZoneRepository zoneRepository,
                        ZoneReportRepository zoneReportRepository, AuditLogRepository auditLogRepository,
                        UserReportRepository userReportRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.alertRepository = alertRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.evidenceRepository = evidenceRepository;
        this.deviceRepository = deviceRepository;
        this.zoneRepository = zoneRepository;
        this.zoneReportRepository = zoneReportRepository;
        this.auditLogRepository = auditLogRepository;
        this.userReportRepository = userReportRepository;
    }

    // Users
    public List<AdminUserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToAdminUserDto)
                .collect(Collectors.toList());
    }

    private AdminUserDto mapToAdminUserDto(User user) {
        var account = accountRepository.findByUserId(user.getId()).orElse(null);
        var userProfile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        
        Long userProfileId = userProfile != null ? userProfile.getId() : null;
        Long alertCount = userProfileId != null ? 
                alertRepository.findByUserProfileId(userProfileId).size() : 0L;
        
        String roleName = null;
        if (user.getRole() != null) {
            // Force load the role name to avoid lazy initialization exception
            roleName = user.getRole().getName();
        }
        
        return new AdminUserDto(
                user.getId(),
                userProfileId,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getTelephone(),
                roleName,
                account != null ? account.getStatus() : null,
                user.getCreatedAt(),
                account != null ? account.getLastAccess() : null,
                alertCount
        );
    }

    // Alerts
    public List<?> getAllAlerts() {
        return alertRepository.findAll();
    }

    // Emergency Contacts
    public List<?> getAllEmergencyContacts() {
        return emergencyContactRepository.findAll();
    }

    // Evidence
    public List<?> getAllEvidence() {
        return evidenceRepository.findAll();
    }

    // Devices
    public List<?> getAllDevices() {
        return deviceRepository.findAll();
    }

    // Zones
    public List<?> getAllZones() {
        return zoneRepository.findAll();
    }

    // Zone Reports
    public List<?> getAllZoneReports() {
        return zoneReportRepository.findAll();
    }

    // Audit Logs
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    // User Reports
    public List<UserReport> getAllUserReports() {
        return userReportRepository.findAll();
    }

    public List<UserReport> getPendingUserReports() {
        return userReportRepository.findByStatus("pending");
    }

    public UserReport updateReportStatus(Long id, String status, Integer adminId) {
        UserReport report = userReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User report not found"));
        report.setStatus(status);
        report.setReviewedByAdminId(adminId);
        return userReportRepository.save(report);
    }
}
