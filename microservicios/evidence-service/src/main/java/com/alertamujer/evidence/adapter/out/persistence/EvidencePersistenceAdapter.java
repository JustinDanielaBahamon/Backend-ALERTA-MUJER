package com.alertamujer.evidence.adapter.out.persistence;

import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output adapter: stores evidence in PostgreSQL with JPA and maps row <-> domain. */
@Component
class EvidencePersistenceAdapter implements EvidenceRepository {

    private final EvidenceJpaRepository jpa;

    EvidencePersistenceAdapter(EvidenceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Evidence save(Evidence evidence) {
        return aDominio(jpa.save(aFila(evidence)));
    }

    @Override
    public Optional<Evidence> find(Long id) {
        return jpa.findById(id).map(EvidencePersistenceAdapter::aDominio);
    }

    @Override
    public List<Evidence> todas() {
        return jpa.findAll().stream().map(EvidencePersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Evidence> ofAlerts(Collection<Long> alertIds) {
        if (alertIds.isEmpty()) return List.of();
        return jpa.findByAlertIdInOrderByCreatedAtDesc(alertIds).stream().map(EvidencePersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Evidence> ofAlert(Long alertId) {
        return jpa.findByAlertIdOrderByCreatedAtAsc(alertId).stream().map(EvidencePersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Evidence> savedBefore(LocalDateTime date) {
        return jpa.findByCreatedAtBefore(date).stream().map(EvidencePersistenceAdapter::aDominio).toList();
    }

    @Override
    public void delete(Evidence evidence) {
        jpa.deleteById(evidence.getId());
    }

    @Override
    public void deleteAll(List<Evidence> evidences) {
        jpa.deleteAllById(evidences.stream().map(Evidence::getId).toList());
    }

    static Evidence aDominio(EvidenceJpaEntity f) {
        Evidence e = new Evidence();
        e.setId(f.getId());
        e.setAlertId(f.getAlertId());
        e.setMediaType(f.getMediaType());
        e.setFileUrl(f.getFileUrl());
        e.setFileName(f.getFileName());
        e.setContentType(f.getContentType());
        e.setSizeBytes(f.getSizeBytes());
        e.setSha256(f.getSha256());
        e.setStorageKey(f.getStorageKey());
        e.setReviewStatus(f.getReviewStatus());
        e.setCreatedAt(f.getCreatedAt());
        return e;
    }

    static EvidenceJpaEntity aFila(Evidence e) {
        EvidenceJpaEntity f = new EvidenceJpaEntity();
        f.setId(e.getId());
        f.setAlertId(e.getAlertId());
        f.setMediaType(e.getMediaType());
        f.setFileUrl(e.getFileUrl());
        f.setFileName(e.getFileName());
        f.setContentType(e.getContentType());
        f.setSizeBytes(e.getSizeBytes());
        f.setSha256(e.getSha256());
        f.setStorageKey(e.getStorageKey());
        f.setReviewStatus(e.getReviewStatus());
        f.setCreatedAt(e.getCreatedAt());
        return f;
    }
}
