package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.evidence.infrastructure.storage.EvidenceStorage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class EvidenceApiService implements EvidenceApi {

    /** Lo justo para reproducir el archivo; la página de seguimiento pide uno nuevo cada vez. */
    static final Duration VIGENCIA_ENLACE = Duration.ofMinutes(10);

    private final EvidenceRepository evidenceRepository;
    private final EvidenceStorage storage;

    public EvidenceApiService(EvidenceRepository evidenceRepository, EvidenceStorage storage) {
        this.evidenceRepository = evidenceRepository;
        this.storage = storage;
    }

    @Override
    public List<EvidenceSummary> evidenciasDeAlerta(Long alertId) {
        return evidenceRepository.findByAlertIdOrderByCreatedAtAsc(alertId).stream()
                .filter(e -> e.getStorageKey() != null)
                .map(e -> new EvidenceSummary(e.getId(), e.getMediaType(), e.getContentType(), e.getSizeBytes(), e.getCreatedAt()))
                .toList();
    }

    @Override
    public Optional<EvidenceFile> archivo(Long alertId, Long evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .filter(e -> e.getAlertId().equals(alertId) && e.getStorageKey() != null)
                .map(this::aArchivo);
    }

    private EvidenceFile aArchivo(Evidence e) {
        return new EvidenceFile(e.getContentType(), e.getFileName(), storage.load(e.getStorageKey()),
                storage.enlaceTemporal(e.getStorageKey(), e.getContentType(), VIGENCIA_ENLACE));
    }
}
