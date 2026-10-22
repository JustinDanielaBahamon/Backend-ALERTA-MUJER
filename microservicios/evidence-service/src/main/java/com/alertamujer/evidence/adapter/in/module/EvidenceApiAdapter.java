package com.alertamujer.evidence.adapter.in.module;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.application.port.out.EvidenceStorage;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Input adapter for other modules (contacts tracking page). Implements the module public API; as a
 * microservice, the same data would be served over HTTP.
 */
@Component
class EvidenceApiAdapter implements EvidenceApi {

    /** Just enough to play the file; the tracking page asks for a new one every time. */
    static final Duration LINK_VALIDITY = Duration.ofMinutes(10);

    private final EvidenceRepository repository;
    private final EvidenceStorage storage;

    EvidenceApiAdapter(EvidenceRepository repository, EvidenceStorage storage) {
        this.repository = repository;
        this.storage = storage;
    }

    @Override
    public List<EvidenceSummary> alertEvidence(Long alertId) {
        return repository.ofAlert(alertId).stream()
                .filter(Evidence::hasFile)
                .map(e -> new EvidenceSummary(e.getId(), e.getMediaType(), e.getContentType(), e.getSizeBytes(), e.getCreatedAt()))
                .toList();
    }

    @Override
    public Optional<EvidenceFile> file(Long alertId, Long evidenceId) {
        return repository.find(evidenceId)
                .filter(e -> e.getAlertId().equals(alertId) && e.hasFile())
                .map(e -> new EvidenceFile(e.getContentType(), e.getFileName(), storage.load(e.getStorageKey()),
                        storage.temporaryLink(e.getStorageKey(), e.getContentType(), LINK_VALIDITY)));
    }
}
