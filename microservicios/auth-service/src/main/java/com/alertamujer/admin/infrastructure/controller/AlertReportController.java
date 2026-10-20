package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AlertReportExporter;
import com.alertamujer.admin.application.service.AlertReportService;
import com.alertamujer.alerts.api.AdminAlertView;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final AlertReportExporter alertReportExporter;

    public AlertReportController(AlertReportService alertReportService, AlertReportExporter alertReportExporter) {
        this.alertReportService = alertReportService;
        this.alertReportExporter = alertReportExporter;
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

    /** Descarga el informe con sus alertas: {@code format=pdf} o {@code format=csv} (se abre en Excel). */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id, @RequestParam(defaultValue = "pdf") String format) {
        boolean pdf = "pdf".equalsIgnoreCase(format);
        if (!pdf && !"csv".equalsIgnoreCase(format)) throw new IllegalArgumentException("Formato no válido: " + format);
        AlertReportService.AlertReportView informe = alertReportService.obtener(id);
        List<AdminAlertView> alertas = alertReportService.alertasDe(id);
        byte[] archivo = pdf ? alertReportExporter.pdf(informe, alertas) : alertReportExporter.csv(informe, alertas);
        alertReportService.registrarExportacion(id);
        return ResponseEntity.ok()
                .contentType(pdf ? MediaType.APPLICATION_PDF : new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("informe-" + id + (pdf ? ".pdf" : ".csv")).build().toString())
                .body(archivo);
    }
}
