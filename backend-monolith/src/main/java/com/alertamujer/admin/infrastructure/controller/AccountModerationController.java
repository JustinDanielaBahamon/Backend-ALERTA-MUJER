package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AccountModerationService;
import com.alertamujer.admin.application.service.AccountModerationService.BloqueoRequest;
import com.alertamujer.admin.application.service.AccountModerationService.HistorialItem;
import com.alertamujer.admin.application.service.AccountModerationService.SolicitudView;
import com.alertamujer.admin.application.service.UserReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Bloqueos con motivo y eliminación de cuentas con aprobación de una segunda administradora. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AccountModerationController {

    private final AccountModerationService service;
    private final UserReportService userReportService;

    public AccountModerationController(AccountModerationService service, UserReportService userReportService) {
        this.service = service;
        this.userReportService = userReportService;
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> block(@PathVariable Long id, @RequestBody BloqueoRequest body,
                                      @AuthenticationPrincipal Long adminId) {
        service.bloquear(id, body, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id, @RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal Long adminId) {
        service.desbloquear(id, body == null ? null : body.get("note"), adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/moderation-history")
    public ResponseEntity<List<HistorialItem>> history(@PathVariable Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    /** Captura adjunta a un caso de moderación (data URI). */
    @GetMapping("/user-reports/{id}/evidence")
    public ResponseEntity<Map<String, String>> evidence(@PathVariable Long id) {
        String evidencia = userReportService.evidencia(id);
        return evidencia == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(Map.of("evidence", evidencia));
    }

    @PostMapping("/users/{id}/deletion-requests")
    public ResponseEntity<SolicitudView> requestDeletion(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                         @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(service.solicitarEliminacion(id, body == null ? null : body.get("reason"), adminId));
    }

    @GetMapping("/deletion-requests")
    public ResponseEntity<List<SolicitudView>> deletionRequests() {
        return ResponseEntity.ok(service.solicitudes());
    }

    @PostMapping("/deletion-requests/{id}/approve")
    public ResponseEntity<SolicitudView> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                                 @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(service.aprobar(id, body == null ? null : body.get("note"), adminId));
    }

    @PostMapping("/deletion-requests/{id}/reject")
    public ResponseEntity<SolicitudView> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                                @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(service.rechazar(id, body == null ? null : body.get("note"), adminId));
    }
}
