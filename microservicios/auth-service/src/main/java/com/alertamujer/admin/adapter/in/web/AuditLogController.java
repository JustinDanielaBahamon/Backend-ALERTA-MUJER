package com.alertamujer.admin.adapter.in.web;

import com.alertamujer.admin.application.port.in.AuditoriaUseCase;
import com.alertamujer.admin.application.port.in.AuditoriaUseCase.AuditActor;
import com.alertamujer.admin.application.port.in.AuditoriaUseCase.AuditPage;
import com.alertamujer.admin.domain.model.FiltroAuditoria;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/** Adaptador de entrada REST: módulo de Auditoría del panel. */
@RestController
@RequestMapping("/api/admin/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditoriaUseCase auditoria;

    public AuditLogController(AuditoriaUseCase auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    public ResponseEntity<AuditPage> list(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditoria.listAll(new FiltroAuditoria(actorId, type, action, from, to), page, size));
    }

    @GetMapping("/actors")
    public ResponseEntity<List<AuditActor>> actors() {
        return ResponseEntity.ok(auditoria.responsables());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] csv = auditoria.exportar(new FiltroAuditoria(actorId, type, action, from, to));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("auditoria-" + LocalDate.now() + ".csv").build().toString())
                .body(csv);
    }
}
