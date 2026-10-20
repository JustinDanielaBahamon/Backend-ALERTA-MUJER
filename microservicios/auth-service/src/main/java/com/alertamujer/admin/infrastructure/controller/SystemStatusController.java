package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.SystemStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Estado del servidor para el panel de administración. */
@RestController
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/api/admin/system-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemStatusService.SystemStatus> systemStatus() {
        return ResponseEntity.ok(systemStatusService.estado());
    }
}
