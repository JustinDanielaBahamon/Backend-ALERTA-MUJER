package com.alertamujer.evidence.infrastructure.controller;

import com.alertamujer.evidence.application.service.EvidenceService;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evidences")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Evidence>> getAllEvidences() {
        return ResponseEntity.ok(evidenceService.getAllEvidences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evidence> getEvidenceById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.getEvidenceById(id, currentUserId));
    }

    @PostMapping
    public ResponseEntity<Evidence> createEvidence(@RequestBody Evidence evidence, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.createEvidence(evidence, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evidence> updateEvidence(@PathVariable Long id, @RequestBody Evidence evidence, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(evidenceService.updateEvidence(id, evidence, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        evidenceService.deleteEvidence(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
