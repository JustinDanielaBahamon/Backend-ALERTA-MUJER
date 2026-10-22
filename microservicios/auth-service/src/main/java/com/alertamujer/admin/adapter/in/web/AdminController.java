package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.adapter.in.web.dto.AdminUserDto;
import com.alertamujer.admin.adapter.in.web.dto.UpdateUserRequest;
import com.alertamujer.admin.adapter.in.web.dto.UserReportRequest;
import com.alertamujer.admin.application.port.in.ManageUsersUseCase;
import com.alertamujer.admin.application.port.in.UserReportsUseCase;
import com.alertamujer.admin.application.port.in.UserReportsUseCase.UserReportView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST input adapter: panel user management and moderation cases. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ManageUsersUseCase users;
    private final UserReportsUseCase reports;

    public AdminController(ManageUsersUseCase users, UserReportsUseCase reports) {
        this.users = users;
        this.reports = reports;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(users.listAll().stream().map(AdminUserDto::of).toList());
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request,
                                                   @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(AdminUserDto.of(users.update(id, request.toChanges(), currentUserId)));
    }

    // ── User reports (moderation) ──────────────────────────

    @GetMapping("/user-reports")
    public ResponseEntity<List<UserReportView>> getAllUserReports() {
        return ResponseEntity.ok(reports.listAll(null));
    }

    @GetMapping("/user-reports/pending")
    public ResponseEntity<List<UserReportView>> getPendingUserReports() {
        return ResponseEntity.ok(reports.listAll("pending"));
    }

    @GetMapping("/user-reports/{id}")
    public ResponseEntity<UserReportView> getUserReport(@PathVariable Long id) {
        return ResponseEntity.ok(reports.get(id));
    }

    @PostMapping("/user-reports")
    public ResponseEntity<UserReportView> createUserReport(@RequestBody UserReportRequest r, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(reports.create(r.reportedUserEmail(), r.reason(), r.description(), r.severity(), currentUserId));
    }

    /** Updates status, severity, reason, description or notes; the reviewer comes from the session. */
    @PutMapping("/user-reports/{id}")
    public ResponseEntity<UserReportView> updateUserReport(@PathVariable Long id, @RequestBody UserReportRequest request,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(reports.update(id, request.toChanges(), currentUserId));
    }

    @DeleteMapping("/user-reports/{id}")
    public ResponseEntity<Void> deleteUserReport(@PathVariable Long id) {
        reports.delete(id);
        return ResponseEntity.noContent().build();
    }
}
