package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AdminService;
import com.alertamujer.admin.application.service.UserReportService;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.controller.dto.AdminUserDto;
import com.alertamujer.admin.infrastructure.controller.dto.UpdateUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserReportService userReportService;

    public AdminController(AdminService adminService, UserReportService userReportService) {
        this.adminService = adminService;
        this.userReportService = userReportService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(adminService.updateUser(id, request, currentUserId));
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

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(adminService.getAllAuditLogs());
    }

    @PostMapping("/audit-logs")
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        return ResponseEntity.ok(adminService.createAuditLog(auditLog));
    }

    // ── Reportes de usuarias (moderación) ──────────────────────────
    @GetMapping("/user-reports")
    public ResponseEntity<List<UserReportService.UserReportView>> getAllUserReports() {
        return ResponseEntity.ok(userReportService.listar(null));
    }

    @GetMapping("/user-reports/pending")
    public ResponseEntity<List<UserReportService.UserReportView>> getPendingUserReports() {
        return ResponseEntity.ok(userReportService.listar("pending"));
    }

    @GetMapping("/user-reports/{id}")
    public ResponseEntity<UserReportService.UserReportView> getUserReport(@PathVariable Long id) {
        return ResponseEntity.ok(userReportService.obtener(id));
    }

    @PostMapping("/user-reports")
    public ResponseEntity<UserReportService.UserReportView> createUserReport(
            @RequestBody UserReportService.UserReportRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(userReportService.crear(request, currentUserId));
    }

    /** Actualiza estado, gravedad, motivo, descripción o notas; la revisora sale de la sesión. */
    @PutMapping("/user-reports/{id}")
    public ResponseEntity<UserReportService.UserReportView> updateUserReport(
            @PathVariable Long id, @RequestBody UserReportService.UserReportRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(userReportService.actualizar(id, request, currentUserId));
    }

    @DeleteMapping("/user-reports/{id}")
    public ResponseEntity<Void> deleteUserReport(@PathVariable Long id) {
        userReportService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
