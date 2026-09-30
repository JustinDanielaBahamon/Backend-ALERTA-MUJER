package com.alertamujer.evidence.infrastructure.controller;

import com.alertamujer.evidence.application.service.EvidenceService;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Evidence>> getAllEvidences() {
        return ResponseEntity.ok(evidenceService.getAllEvidences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evidence> getEvidenceById(@PathVariable Long id) {
        return ResponseEntity.ok(evidenceService.getEvidenceById(id));
    }

    @PostMapping
    public ResponseEntity<Evidence> createEvidence(@RequestBody Evidence evidence) {
        return ResponseEntity.ok(evidenceService.createEvidence(evidence));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evidence> updateEvidence(@PathVariable Long id, @RequestBody Evidence evidence) {
        return ResponseEntity.ok(evidenceService.updateEvidence(id, evidence));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id) {
        evidenceService.deleteEvidence(id);
        return ResponseEntity.ok().build();
    }
}
