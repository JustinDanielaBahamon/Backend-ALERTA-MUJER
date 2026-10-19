package com.alertamujer.evidence.infrastructure.controller;

import com.alertamujer.evidence.application.service.AdminEvidenceService;
import com.alertamujer.evidence.application.service.EvidenceService;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.controller.dto.EvidenceView;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/evidences")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final AdminEvidenceService adminEvidenceService;

    public EvidenceController(EvidenceService evidenceService, AdminEvidenceService adminEvidenceService) {
        this.evidenceService = evidenceService;
        this.adminEvidenceService = adminEvidenceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminEvidenceService.AdminEvidenceView>> getAllEvidences() {
        return ResponseEntity.ok(adminEvidenceService.listar());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEvidenceService.EvidenceStats> getStats() {
        return ResponseEntity.ok(adminEvidenceService.estadisticas());
    }

    /** Revisión de la administración: pending, verified o rejected. */
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEvidenceService.AdminEvidenceView> review(@PathVariable Long id,
                                                                      @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(adminEvidenceService.cambiarRevision(id, body.get("reviewStatus")));
    }

    /** Evidencias de la usuaria autenticada (más recientes primero). */
    @GetMapping("/me")
    public ResponseEntity<List<EvidenceView>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.getMine(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evidence> getEvidenceById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.getEvidenceById(id, currentUserId));
    }

    /** Sube una foto, video, audio o PDF y lo asocia a una alerta de la usuaria. */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceView> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam("alertId") Long alertId,
                                               @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.upload(file, alertId, currentUserId));
    }

    /** Contenido del archivo, solo para la dueña. Admite Range para adelantar videos y audios. */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id,
                                            @RequestParam(defaultValue = "false") boolean download,
                                            @AuthenticationPrincipal Long currentUserId,
                                            Authentication authentication) {
        EvidenceService.StoredEvidence stored = esAdmin(authentication)
                ? evidenceService.getFileAsAdmin(id)
                : evidenceService.getFile(id, currentUserId);
        Evidence e = stored.evidence();
        boolean inline = !download && !"document".equals(e.getMediaType());
        ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(e.getFileName() == null ? "evidencia-" + id : e.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(e.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(stored.resource());
    }

    @PostMapping
    public ResponseEntity<Evidence> createEvidence(@RequestBody Evidence evidence, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.createEvidence(evidence, currentUserId));
    }

    /** La dueña le pone un nombre más corto a su evidencia. */
    @PatchMapping("/{id}/name")
    public ResponseEntity<EvidenceView> rename(@PathVariable Long id, @RequestBody java.util.Map<String, String> body,
                                               @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.renombrar(id, body == null ? null : body.get("name"), currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evidence> updateEvidence(@PathVariable Long id, @RequestBody Evidence evidence, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.updateEvidence(id, evidence, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                               Authentication authentication) {
        if (esAdmin(authentication)) {
            evidenceService.deleteEvidenceAsAdmin(id);
        } else {
            evidenceService.deleteEvidence(id, currentUserId);
        }
        return ResponseEntity.noContent().build();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
