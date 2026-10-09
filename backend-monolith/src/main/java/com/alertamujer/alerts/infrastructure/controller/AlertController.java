package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.service.AlertService;
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

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return alertService.getAlertById(id, currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<Alert>> getAlertsByUser(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(alertService.getAlertsByUserProfile(userProfileId));
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<List<Alert>> getAlertsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.getAlertsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertService.createAlert(alert, currentUserId));
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
