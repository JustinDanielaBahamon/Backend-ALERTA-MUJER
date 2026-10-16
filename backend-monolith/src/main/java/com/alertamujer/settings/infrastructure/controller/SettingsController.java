package com.alertamujer.settings.infrastructure.controller;

import com.alertamujer.settings.application.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Parámetros de la app: los edita la administración y los leen las apps con sesión. */
@RestController
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsService.SettingsView> get() {
        return ResponseEntity.ok(settingsService.obtener());
    }

    @PutMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsService.SettingsView> update(@RequestBody SettingsService.SettingsRequest request,
                                                               @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(settingsService.actualizar(request, currentUserId));
    }

    @GetMapping("/api/settings")
    public ResponseEntity<SettingsService.PublicSettings> publicSettings() {
        return ResponseEntity.ok(settingsService.publicos());
    }
}
