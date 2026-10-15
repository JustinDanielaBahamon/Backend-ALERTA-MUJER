package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AlertReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Informes de alertas del panel de administración (RF13.4). */
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AlertReportController {

    private final AlertReportService alertReportService;

    public AlertReportController(AlertReportService alertReportService) {
        this.alertReportService = alertReportService;
    }

    @GetMapping
    public ResponseEntity<List<AlertReportService.AlertReportView>> list() {
        return ResponseEntity.ok(alertReportService.listar());
    }

    @GetMapping("/stats")
    public ResponseEntity<AlertReportService.ReportStats> stats() {
        return ResponseEntity.ok(alertReportService.estadisticas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertReportService.AlertReportView> get(@PathVariable Long id) {
        return ResponseEntity.ok(alertReportService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<AlertReportService.AlertReportView> create(@RequestBody AlertReportService.AlertReportRequest request,
                                                                     @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alertReportService.crear(request, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertReportService.AlertReportView> update(@PathVariable Long id,
                                                                     @RequestBody AlertReportService.AlertReportRequest request) {
        return ResponseEntity.ok(alertReportService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alertReportService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
