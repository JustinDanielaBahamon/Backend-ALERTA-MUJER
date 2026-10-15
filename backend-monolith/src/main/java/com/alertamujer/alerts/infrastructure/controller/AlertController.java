package com.alertamujer.alerts.infrastructure.controller;

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

    public AlertController(AlertService alertService, LocationLogService locationLogService,
                           AlertContactService alertContactService, AdminAlertService adminAlertService) {
        this.alertService = alertService;
        this.locationLogService = locationLogService;
        this.alertContactService = alertContactService;
        this.adminAlertService = adminAlertService;
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
    public ResponseEntity<List<AdminAlertService.AdminAlertView>> getAllAlerts() {
        return ResponseEntity.ok(adminAlertService.listar());
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
        Alert creada = alertService.createAlert(alert, currentUserId);
        alertContactService.registrarDestinatarios(creada);
        return ResponseEntity.ok(creada);
    }

    /** Contactos avisados en una alerta y el estado de cada canal (solo la dueña de la alerta). */
    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<AlertContactService.ContactoNotificado>> getAlertContacts(
            @PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return alertService.getAlertById(id, currentUserId)
                .map(alert -> ResponseEntity.ok(alertContactService.contactosDeAlerta(alert)))
                .orElse(ResponseEntity.notFound().build());
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
