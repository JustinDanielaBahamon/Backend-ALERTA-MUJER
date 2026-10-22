package com.alertamujer.evidence.adapter.in.http;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Input adapter for Alert Service (contacts tracking page). Protected by the internal key
 * (ClaveInternaFilter). Returns the data and the signed link separately from the content.
 */
@RestController
@RequestMapping("/internal/evidence/alerts/{alertId}")
class InternalEvidenceController {

    private final EvidenceApi evidences;

    InternalEvidenceController(EvidenceApi evidences) {
        this.evidences = evidences;
    }

    @GetMapping
    List<EvidenceApi.EvidenceSummary> ofAlert(@PathVariable Long alertId) {
        return evidences.alertEvidence(alertId);
    }

    @GetMapping("/evidences/{id}")
    ResponseEntity<FileData> data(@PathVariable Long alertId, @PathVariable Long id) {
        return evidences.file(alertId, id)
                .map(a -> ResponseEntity.ok(new FileData(a.contentType(), a.fileName(),
                        a.temporaryLink().map(Object::toString).orElse(null))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/evidences/{id}/content")
    ResponseEntity<Resource> content(@PathVariable Long alertId, @PathVariable Long id) {
        return evidences.file(alertId, id)
                .map((EvidenceFile a) -> ResponseEntity.ok().contentType(MediaType.parseMediaType(a.contentType())).body(a.resource()))
                .orElse(ResponseEntity.notFound().build());
    }

    record FileData(String contentType, String fileName, String temporaryLink) {}
}
