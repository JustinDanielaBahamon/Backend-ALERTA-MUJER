package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AdminService;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.UserReport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<?>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<?>> getAllAlerts() {
        return ResponseEntity.ok(adminService.getAllAlerts());
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<?>> getAllEmergencyContacts() {
        return ResponseEntity.ok(adminService.getAllEmergencyContacts());
    }

    @GetMapping("/evidences")
    public ResponseEntity<List<?>> getAllEvidence() {
        return ResponseEntity.ok(adminService.getAllEvidence());
    }

    @GetMapping("/devices")
    public ResponseEntity<List<?>> getAllDevices() {
        return ResponseEntity.ok(adminService.getAllDevices());
    }

    @GetMapping("/zones")
    public ResponseEntity<List<?>> getAllZones() {
        return ResponseEntity.ok(adminService.getAllZones());
    }

    @GetMapping("/reports")
    public ResponseEntity<List<?>> getAllZoneReports() {
        return ResponseEntity.ok(adminService.getAllZoneReports());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(adminService.getAllAuditLogs());
    }

    @PostMapping("/audit-logs")
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        return ResponseEntity.ok(adminService.createAuditLog(auditLog));
    }

    @GetMapping("/user-reports")
    public ResponseEntity<List<UserReport>> getAllUserReports() {
        return ResponseEntity.ok(adminService.getAllUserReports());
    }

    @GetMapping("/user-reports/pending")
    public ResponseEntity<List<UserReport>> getPendingUserReports() {
        return ResponseEntity.ok(adminService.getPendingUserReports());
    }

    @PutMapping("/user-reports/{id}")
    public ResponseEntity<UserReport> updateReportStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String status = (String) request.get("status");
        Integer adminId = (Integer) request.get("adminId");
        return ResponseEntity.ok(adminService.updateReportStatus(id, status, adminId));
    }
}
