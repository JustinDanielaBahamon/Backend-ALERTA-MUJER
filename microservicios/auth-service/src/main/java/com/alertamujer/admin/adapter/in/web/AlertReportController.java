package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.adapter.in.web.dto.AlertReportRequest;
import com.alertamujer.admin.application.port.in.ReportExportUseCase;
import com.alertamujer.admin.application.port.in.ReportExportUseCase.AlertReportView;
import com.alertamujer.admin.application.port.in.ReportExportUseCase.ReportStats;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** REST input adapter: panel alert reports (RF13.4). */
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AlertReportController {

    private final ReportExportUseCase reportExports;

    public AlertReportController(ReportExportUseCase reportExports) {
        this.reportExports = reportExports;
    }

    @GetMapping
    public ResponseEntity<List<AlertReportView>> list() {
        return ResponseEntity.ok(reportExports.listAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<ReportStats> stats() {
        return ResponseEntity.ok(reportExports.stats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertReportView> get(@PathVariable Long id) {
        return ResponseEntity.ok(reportExports.get(id));
    }

    @PostMapping
    public ResponseEntity<AlertReportView> create(@RequestBody AlertReportRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(reportExports.create(request.toChanges(), currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertReportView> update(@PathVariable Long id, @RequestBody AlertReportRequest request) {
        return ResponseEntity.ok(reportExports.update(id, request.toChanges()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportExports.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Downloads the report with its alerts: {@code format=pdf} or {@code format=csv} (opens in Excel). */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id, @RequestParam(defaultValue = "pdf") String format) {
        boolean pdf = "pdf".equalsIgnoreCase(format);
        if (!pdf && !"csv".equalsIgnoreCase(format)) throw new IllegalArgumentException("Formato no válido: " + format);
        byte[] file = reportExports.export(id, pdf);
        return ResponseEntity.ok()
                .contentType(pdf ? MediaType.APPLICATION_PDF : new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("informe-" + id + (pdf ? ".pdf" : ".csv")).build().toString())
                .body(file);
    }
}
