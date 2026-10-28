package com.alertamujer.evidence.api;

import org.springframework.core.io.Resource;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** What other modules can query about evidence (e.g. the contacts tracking page). */
public interface EvidenceApi {

    /** Evidence with a file of an alert, oldest to newest. */
    List<EvidenceSummary> alertEvidence(Long alertId);

    /** File of an evidence, only if it belongs to that alert. */
    Optional<EvidenceFile> file(Long alertId, Long evidenceId);

    record EvidenceSummary(Long id, String mediaType, String contentType, Long sizeBytes, LocalDateTime createdAt) {}

    /** temporaryLink: open it straight from the storage (S3) if available; otherwise serve resource. */
    record EvidenceFile(String contentType, String fileName, Resource resource, Optional<URI> temporaryLink) {}
}
