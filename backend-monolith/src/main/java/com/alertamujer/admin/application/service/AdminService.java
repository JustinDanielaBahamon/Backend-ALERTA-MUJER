package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.controller.dto.AdminUserDto;
import com.alertamujer.admin.infrastructure.controller.dto.UpdateUserRequest;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.admin.infrastructure.repository.UserReportRepository;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.zones.infrastructure.repository.ZoneRepository;
import com.alertamujer.zones.infrastructure.repository.ZoneReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    /**
     * Update user information.
     * Validates account status values and prevents admins from blocking themselves.
     *
     * @param userId The ID of the user to update
     * @param request The update request with optional fields
     * @param currentUserId The ID of the admin performing the update
     * @return Updated user DTO
     */
    public AdminUserDto updateUser(Long userId, UpdateUserRequest request, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Account account = accountRepository.findByUserId(userId).orElse(null);
        
        // Validate and update account status
        if (request.accountStatus() != null) {
            validateAccountStatus(request.accountStatus());
            
            // Prevent admin from blocking themselves
            if (userId.equals(currentUserId) && "blocked".equals(request.accountStatus())) {
                throw new IllegalArgumentException("Cannot block your own account");
            }
            
            if (account != null) {
                account.setStatus(request.accountStatus());
                accountRepository.save(account);
            }
        }
        
        // Update user fields
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.telephone() != null) {
            user.setTelephone(request.telephone());
        }
        if (request.email() != null) {
            // Check if email is already taken by another user
            Optional<User> existingUser = userRepository.findByEmail(request.email());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.email());
        }
        
        userRepository.save(user);
        
        return mapToAdminUserDto(user);
    }
    
    private void validateAccountStatus(String status) {
        Set<String> validStatuses = Set.of("active", "inactive", "blocked", "pending");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid account status. Must be one of: active, inactive, blocked, pending");
        }
    }
}