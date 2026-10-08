package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final AlertRepository alertRepository;

    public EvidenceService(EvidenceRepository evidenceRepository, AlertRepository alertRepository) {
        this.evidenceRepository = evidenceRepository;
        this.alertRepository = alertRepository;
    }

    public List<Evidence> getAllEvidences() {
        return evidenceRepository.findAll();
    }

    public Evidence getEvidenceById(Long id) {
        return evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
    }

    public Evidence getEvidenceById(Long id, Long currentUserId) {
        Evidence evidence = getEvidenceById(id);
        validateOwnership(evidence, currentUserId);
        return evidence;
    }

    public Evidence createEvidence(Evidence evidence, Long currentUserId) {
        Alert alert = alertRepository.findById(evidence.getAlertId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert", evidence.getAlertId()));
        
        if (!alert.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes agregar evidencias a alertas de otro usuario");
        }
        return evidenceRepository.save(evidence);
    }

    public Evidence updateEvidence(Long id, Evidence evidence, Long currentUserId) {
        Evidence existing = getEvidenceById(id, currentUserId);
        existing.setMediaType(evidence.getMediaType());
        existing.setFileUrl(evidence.getFileUrl());
        return evidenceRepository.save(existing);
    }

    public void deleteEvidence(Long id, Long currentUserId) {
        Evidence evidence = getEvidenceById(id, currentUserId);
        evidenceRepository.deleteById(id);
    }

    private void validateOwnership(Evidence evidence, Long currentUserId) {
        Alert alert = alertRepository.findById(evidence.getAlertId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert", evidence.getAlertId()));
        if (!alert.getUserProfileId().equals(currentUserId)) {
            throw new AccessDeniedException("No puedes acceder a evidencias de alertas de otro usuario");
        }
    }
}
