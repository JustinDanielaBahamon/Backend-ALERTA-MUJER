package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.application.port.in.AccountModerationUseCase;
import com.alertamujer.admin.application.port.in.AccountModerationUseCase.Block;
import com.alertamujer.admin.application.port.in.AccountModerationUseCase.HistoryItem;
import com.alertamujer.admin.application.port.in.AccountModerationUseCase.DeletionRequestView;
import com.alertamujer.admin.application.port.in.UserReportsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Adaptador de entrada REST: bloqueos con motivo y eliminación de cuentas con aprobación de otra administradora. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AccountModerationController {

    private final AccountModerationUseCase moderation;
    private final UserReportsUseCase reportes;

    public AccountModerationController(AccountModerationUseCase moderation, UserReportsUseCase reportes) {
        this.moderation = moderation;
        this.reportes = reportes;
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> block(@PathVariable Long id, @RequestBody Block body, @AuthenticationPrincipal Long adminId) {
        moderation.block(id, body, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id, @RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal Long adminId) {
        moderation.unblock(id, body == null ? null : body.get("note"), adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/moderation-history")
    public ResponseEntity<List<HistoryItem>> history(@PathVariable Long id) {
        return ResponseEntity.ok(moderation.history(id));
    }

    /** Captura adjunta a un caso de moderación (data URI). */
    @GetMapping("/user-reports/{id}/evidence")
    public ResponseEntity<Map<String, String>> evidence(@PathVariable Long id) {
        String evidence = reportes.evidence(id);
        return evidence == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(Map.of("evidence", evidence));
    }

    @PostMapping("/users/{id}/deletion-requests")
    public ResponseEntity<DeletionRequestView> requestDeletion(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                         @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(moderation.requestDeletion(id, body == null ? null : body.get("reason"), adminId));
    }

    @GetMapping("/deletion-requests")
    public ResponseEntity<List<DeletionRequestView>> deletionRequests() {
        return ResponseEntity.ok(moderation.solicitudes());
    }

    @PostMapping("/deletion-requests/{id}/approve")
    public ResponseEntity<DeletionRequestView> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                                 @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(moderation.approve(id, body == null ? null : body.get("note"), adminId));
    }

    @PostMapping("/deletion-requests/{id}/reject")
    public ResponseEntity<DeletionRequestView> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                                @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(moderation.reject(id, body == null ? null : body.get("note"), adminId));
    }
}
