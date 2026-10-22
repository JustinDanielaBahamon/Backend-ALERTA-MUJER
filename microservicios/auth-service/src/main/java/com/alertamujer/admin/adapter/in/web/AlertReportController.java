package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.adapter.in.web.dto.AlertReportRequest;
import com.alertamujer.admin.application.port.in.InformesUseCase;
import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.admin.application.port.in.InformesUseCase.ReportStats;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Adaptador de entrada REST: informes de alertas del panel (RF13.4). */
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AlertReportController {

    private final InformesUseCase informes;

    public AlertReportController(InformesUseCase informes) {
        this.informes = informes;
    }

    @GetMapping
    public ResponseEntity<List<AlertReportView>> list() {
        return ResponseEntity.ok(informes.listAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<ReportStats> stats() {
        return ResponseEntity.ok(informes.stats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertReportView> get(@PathVariable Long id) {
        return ResponseEntity.ok(informes.obtener(id));
    }

    @PostMapping
    public ResponseEntity<AlertReportView> create(@RequestBody AlertReportRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(informes.create(request.aCambios(), currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertReportView> update(@PathVariable Long id, @RequestBody AlertReportRequest request) {
        return ResponseEntity.ok(informes.actualizar(id, request.aCambios()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        informes.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Descarga el informe con sus alertas: {@code format=pdf} o {@code format=csv} (se abre en Excel). */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id, @RequestParam(defaultValue = "pdf") String format) {
        boolean pdf = "pdf".equalsIgnoreCase(format);
        if (!pdf && !"csv".equalsIgnoreCase(format)) throw new IllegalArgumentException("Formato no válido: " + format);
        byte[] file = informes.exportar(id, pdf);
        return ResponseEntity.ok()
                .contentType(pdf ? MediaType.APPLICATION_PDF : new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("informe-" + id + (pdf ? ".pdf" : ".csv")).build().toString())
                .body(file);
    }
}
