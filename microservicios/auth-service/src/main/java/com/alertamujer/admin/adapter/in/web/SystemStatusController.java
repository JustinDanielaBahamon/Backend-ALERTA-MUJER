package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.application.port.in.EstadoSistemaUseCase;
import com.alertamujer.admin.application.port.in.EstadoSistemaUseCase.SystemStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: estado del servidor para el panel de administración. */
@RestController
public class SystemStatusController {

    private final EstadoSistemaUseCase estadoSistema;

    public SystemStatusController(EstadoSistemaUseCase estadoSistema) {
        this.estadoSistema = estadoSistema;
    }

    @GetMapping("/api/admin/system-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemStatus> systemStatus() {
        return ResponseEntity.ok(estadoSistema.estado());
    }
}
