package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AdminSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Parámetros de la app: los edita la administración y los leen las apps con sesión. */
@RestController
public class AdminSettingsController {

    private final AdminSettingsService settingsService;

    public AdminSettingsController(AdminSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSettingsService.SettingsView> get() {
        return ResponseEntity.ok(settingsService.obtener());
    }

    @PutMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSettingsService.SettingsView> update(@RequestBody AdminSettingsService.SettingsRequest request,
                                                                    @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(settingsService.actualizar(request, currentUserId));
    }

    @GetMapping("/api/settings")
    public ResponseEntity<AdminSettingsService.PublicSettings> publicSettings() {
        return ResponseEntity.ok(settingsService.publicos());
    }
}
