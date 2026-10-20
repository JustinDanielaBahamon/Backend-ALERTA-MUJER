package com.alertamujer.settings.adapter.in.web;

import com.alertamujer.settings.adapter.in.web.dto.PublicSettings;
import com.alertamujer.settings.adapter.in.web.dto.SettingsRequest;
import com.alertamujer.settings.adapter.in.web.dto.SettingsView;
import com.alertamujer.settings.application.port.in.AdministrarAjustesUseCase;
import com.alertamujer.settings.application.port.in.ConsultarAjustesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Adaptador de entrada REST: la administración edita los parámetros y las apps con sesión los leen. */
@RestController
public class SettingsController {

    private final AdministrarAjustesUseCase administracion;
    private final ConsultarAjustesUseCase consultas;

    public SettingsController(AdministrarAjustesUseCase administracion, ConsultarAjustesUseCase consultas) {
        this.administracion = administracion;
        this.consultas = consultas;
    }

    @GetMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsView> get() {
        return ResponseEntity.ok(SettingsView.of(administracion.obtener()));
    }

    @PutMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsView> update(@RequestBody SettingsRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(SettingsView.of(administracion.actualizar(request.aCambios(), currentUserId)));
    }

    @GetMapping("/api/settings")
    public ResponseEntity<PublicSettings> publicSettings() {
        return ResponseEntity.ok(PublicSettings.of(consultas.publicos()));
    }
}
