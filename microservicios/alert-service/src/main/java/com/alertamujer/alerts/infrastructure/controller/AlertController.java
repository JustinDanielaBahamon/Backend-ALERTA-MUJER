package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.application.service.AlertMessageService;
import com.alertamujer.alerts.application.service.AdminAlertService;
import com.alertamujer.alerts.application.service.AlertContactService;
import com.alertamujer.alerts.application.service.AlertService;
import com.alertamujer.alerts.application.service.LocationLogService;
import com.alertamujer.alerts.domain.model.LocationLog;
import org.springframework.security.core.Authentication;
import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final LocationLogService locationLogService;
    private final AlertContactService alertContactService;
    private final AdminAlertService adminAlertService;
    private final AlertMessageService alertMessageService;

    public AlertController(AlertService alertService, LocationLogService locationLogService,
                           AlertContactService alertContactService, AdminAlertService adminAlertService, AlertMessageService alertMessageService) {
        this.alertService = alertService;
        this.locationLogService = locationLogService;
        this.alertContactService = alertContactService;
        this.adminAlertService = adminAlertService;
        this.alertMessageService = alertMessageService;
    }

    /** Recorrido GPS de una alerta (lo usa el seguimiento de alertas del móvil). */
    @GetMapping("/{id}/locations")
    public ResponseEntity<List<LocationLog>> getAlertLocations(@PathVariable Long id,
                                                               @AuthenticationPrincipal Long currentUserId,
                                                               Authentication authentication) {
        return ResponseEntity.ok(locationLogService.getByAlert(id, currentUserId, LocationLogController.isAdmin(authentication)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminAlertView>> getAllAlerts() {
        return ResponseEntity.ok(adminAlertService.listar());
    }

    /** Cambia el estado de una alerta de cualquier usuaria, por ejemplo a "resolved" (atendida). */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Alert> changeAlertStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(adminAlertService.cambiarEstado(id, body.get("status")));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Alert>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertService.getMine(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return alertService.getAlertById(id, currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<Alert>> getAlertsByUser(
            @PathVariable Long userProfileId, 
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertService.getAlertsByUserProfile(userProfileId, currentUserId));
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertService.createAlert(alert, currentUserId));
    }

    /** Contactos avisados en una alerta y el estado de cada canal (solo la dueña de la alerta). */
    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<AlertContactService.ContactoNotificado>> getAlertContacts(
            @PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return alertService.getAlertById(id, currentUserId)
                .map(alert -> ResponseEntity.ok(alertContactService.contactosDeAlerta(alert)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Chat de la alerta (solo la dueña). after: id del último mensaje que ya tiene (0 = todos). */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AlertMessageView>> getMessages(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "0") Long after,
                                                              @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertMessageService.mensajesDeLaDuena(id, currentUserId, after));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AlertMessageView> sendMessage(@PathVariable Long id, @RequestBody MensajeRequest request,
                                                        @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertMessageService.enviarDeLaDuena(id, currentUserId, request.body()));
    }

    public record MensajeRequest(String body) {}

    /** Conservar las evidencias de la alerta (no se borran a los 60 días). Body: {"keep": true}. */
    @PatchMapping("/{id}/keep-evidence")
    public ResponseEntity<Alert> keepEvidence(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body,
                                              @AuthenticationPrincipal Long currentUserId) {
        boolean conservar = body != null && Boolean.TRUE.equals(body.get("keep"));
        return ResponseEntity.ok(alertService.conservarEvidencias(id, conservar, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alert> updateAlert(@PathVariable Long id, @RequestBody Alert alert, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertService.updateAlert(id, alert, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        alertService.deleteAlert(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
