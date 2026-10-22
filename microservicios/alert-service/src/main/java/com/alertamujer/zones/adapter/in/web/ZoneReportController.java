package com.alertamujer.zones.adapter.in.web;

import com.alertamujer.zones.adapter.in.web.dto.ZoneReportRequest;
import com.alertamujer.zones.adapter.in.web.dto.ZoneReportResponse;
import com.alertamujer.zones.application.port.in.ReportesZonaUseCase;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Adaptador de entrada REST de los reportes de zonas. */
@RestController
@RequestMapping("/api/zone-reports")
public class ZoneReportController {

    private final ReportesZonaUseCase reportes;

    public ZoneReportController(ReportesZonaUseCase reportes) {
        this.reportes = reportes;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ZoneReportResponse>> getAllReports() {
        return ResponseEntity.ok(vistas(reportes.todos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneReportResponse> getReportById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return reportes.uno(id, currentUserId).map(r -> ResponseEntity.ok(ZoneReportResponse.of(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<ZoneReportResponse>> getReportsByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(vistas(reportes.deZona(zoneId)));
    }

    /** Reportes de una usuaria (id de usuaria o de perfil): los propios, o cualquiera para la administración. */
    @GetMapping("/my/{userProfileId}")
    public ResponseEntity<List<ZoneReportResponse>> getMyReports(@PathVariable Long userProfileId,
                                                                 @AuthenticationPrincipal Long currentUserId,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(vistas(reportes.deUsuaria(userProfileId, currentUserId, esAdmin(authentication))));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ZoneReportResponse>> getReportsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(vistas(reportes.conEstado(status)));
    }

    @PostMapping
    public ResponseEntity<ZoneReportResponse> createReport(@RequestBody ZoneReportRequest request,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ZoneReportResponse.of(
                reportes.create(request.zoneId(), request.classification(), request.comment(), currentUserId)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZoneReportResponse> approveReport(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(ZoneReportResponse.of(reportes.aprobar(id, request.get("adminId"))));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZoneReportResponse> rejectReport(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(ZoneReportResponse.of(reportes.rechazar(id, request.get("adminId"))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        reportes.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    private static List<ZoneReportResponse> vistas(List<ZoneReport> list) {
        return list.stream().map(ZoneReportResponse::of).toList();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
