package com.alertamujer.admin.infrastructure.controller;

import com.alertamujer.admin.application.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Historial de cambios para el panel de administración. */
@RestController
@RequestMapping("/api/admin/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogService.AuditLogView>> list(@RequestParam(required = false) String entityType,
                                                                   @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(auditLogService.listar(entityType, limit));
    }
}
