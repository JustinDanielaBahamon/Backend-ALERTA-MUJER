package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.adapter.in.web.dto.AdminUserDto;
import com.alertamujer.admin.adapter.in.web.dto.UpdateUserRequest;
import com.alertamujer.admin.adapter.in.web.dto.UserReportRequest;
import com.alertamujer.admin.application.port.in.GestionUsuariasUseCase;
import com.alertamujer.admin.application.port.in.ReportesUsuariaUseCase;
import com.alertamujer.admin.application.port.in.ReportesUsuariaUseCase.UserReportView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST: gestión de usuarias y casos de moderación del panel. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GestionUsuariasUseCase usuarias;
    private final ReportesUsuariaUseCase reportes;

    public AdminController(GestionUsuariasUseCase usuarias, ReportesUsuariaUseCase reportes) {
        this.usuarias = usuarias;
        this.reportes = reportes;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(usuarias.listar().stream().map(AdminUserDto::of).toList());
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request,
                                                   @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(AdminUserDto.of(usuarias.actualizar(id, request.aCambios(), currentUserId)));
    }

    // ── Reportes de usuarias (moderación) ──────────────────────────

    @GetMapping("/user-reports")
    public ResponseEntity<List<UserReportView>> getAllUserReports() {
        return ResponseEntity.ok(reportes.listar(null));
    }

    @GetMapping("/user-reports/pending")
    public ResponseEntity<List<UserReportView>> getPendingUserReports() {
        return ResponseEntity.ok(reportes.listar("pending"));
    }

    @GetMapping("/user-reports/{id}")
    public ResponseEntity<UserReportView> getUserReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportes.obtener(id));
    }

    @PostMapping("/user-reports")
    public ResponseEntity<UserReportView> createUserReport(@RequestBody UserReportRequest r, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(reportes.crear(r.reportedUserEmail(), r.reason(), r.description(), r.severity(), currentUserId));
    }

    /** Actualiza estado, gravedad, motivo, descripción o notas; la revisora sale de la sesión. */
    @PutMapping("/user-reports/{id}")
    public ResponseEntity<UserReportView> updateUserReport(@PathVariable Long id, @RequestBody UserReportRequest request,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(reportes.actualizar(id, request.aCambios(), currentUserId));
    }

    @DeleteMapping("/user-reports/{id}")
    public ResponseEntity<Void> deleteUserReport(@PathVariable Long id) {
        reportes.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
