package com.alertamujer.zones.infrastructure.controller;

import com.alertamujer.zones.application.service.ZoneReportService;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zone-reports")
public class ZoneReportController {

    private final ZoneReportService zoneReportService;

    public ZoneReportController(ZoneReportService zoneReportService) {
        this.zoneReportService = zoneReportService;
    }

    @GetMapping
    public ResponseEntity<List<ZoneReport>> getAllReports() {
        return ResponseEntity.ok(zoneReportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneReport> getReportById(@PathVariable Long id) {
        return zoneReportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<ZoneReport>> getReportsByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(zoneReportService.getReportsByZone(zoneId));
    }

    @GetMapping("/my/{userProfileId}")
    public ResponseEntity<List<ZoneReport>> getMyReports(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(zoneReportService.getReportsByUserProfile(userProfileId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ZoneReport>> getReportsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(zoneReportService.getReportsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<ZoneReport> createReport(@RequestBody ZoneReport report) {
        return ResponseEntity.ok(zoneReportService.createReport(report));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ZoneReport> approveReport(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(zoneReportService.approveReport(id, request.get("adminId")));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ZoneReport> rejectReport(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(zoneReportService.rejectReport(id, request.get("adminId")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        zoneReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
