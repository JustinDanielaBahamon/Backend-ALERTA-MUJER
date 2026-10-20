package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase;
import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase.Bloqueo;
import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase.HistorialItem;
import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase.SolicitudView;
import com.alertamujer.admin.application.port.in.ReportesUsuariaUseCase;
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

    private final ModeracionCuentasUseCase moderacion;
    private final ReportesUsuariaUseCase reportes;

    public AccountModerationController(ModeracionCuentasUseCase moderacion, ReportesUsuariaUseCase reportes) {
        this.moderacion = moderacion;
        this.reportes = reportes;
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> block(@PathVariable Long id, @RequestBody Bloqueo body, @AuthenticationPrincipal Long adminId) {
        moderacion.bloquear(id, body, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id, @RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal Long adminId) {
        moderacion.desbloquear(id, body == null ? null : body.get("note"), adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/moderation-history")
    public ResponseEntity<List<HistorialItem>> history(@PathVariable Long id) {
        return ResponseEntity.ok(moderacion.historial(id));
    }

    /** Captura adjunta a un caso de moderación (data URI). */
    @GetMapping("/user-reports/{id}/evidence")
    public ResponseEntity<Map<String, String>> evidence(@PathVariable Long id) {
        String evidencia = reportes.evidencia(id);
        return evidencia == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(Map.of("evidence", evidencia));
    }

    @PostMapping("/users/{id}/deletion-requests")
    public ResponseEntity<SolicitudView> requestDeletion(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                         @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(moderacion.solicitarEliminacion(id, body == null ? null : body.get("reason"), adminId));
    }

    @GetMapping("/deletion-requests")
    public ResponseEntity<List<SolicitudView>> deletionRequests() {
        return ResponseEntity.ok(moderacion.solicitudes());
    }

    @PostMapping("/deletion-requests/{id}/approve")
    public ResponseEntity<SolicitudView> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                                 @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(moderacion.aprobar(id, body == null ? null : body.get("note"), adminId));
    }

    @PostMapping("/deletion-requests/{id}/reject")
    public ResponseEntity<SolicitudView> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                                @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(moderacion.rechazar(id, body == null ? null : body.get("note"), adminId));
    }
}
