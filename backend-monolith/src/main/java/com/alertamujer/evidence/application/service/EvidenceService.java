package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;

    public EvidenceService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    public List<Evidence> getAllEvidences() {
        return evidenceRepository.findAll();
    }

    public Evidence getEvidenceById(Long id) {
        return evidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));
    }

    public Evidence createEvidence(Evidence evidence) {
        return evidenceRepository.save(evidence);
    }

    public Evidence updateEvidence(Long id, Evidence evidence) {
        Evidence existing = getEvidenceById(id);
        existing.setMediaType(evidence.getMediaType());
        existing.setFileUrl(evidence.getFileUrl());
        return evidenceRepository.save(existing);
    }

    public void deleteEvidence(Long id) {
        evidenceRepository.deleteById(id);
    }
}
