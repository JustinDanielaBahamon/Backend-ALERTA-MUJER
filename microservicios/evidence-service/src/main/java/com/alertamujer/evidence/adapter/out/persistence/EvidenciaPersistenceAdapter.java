package com.alertamujer.evidence.adapter.out.persistence;

import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: guarda las evidencias en PostgreSQL con JPA y traduce fila ↔ dominio. */
@Component
class EvidenciaPersistenceAdapter implements EvidenciaRepositorio {

    private final EvidenceJpaRepository jpa;

    EvidenciaPersistenceAdapter(EvidenceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Evidence guardar(Evidence evidencia) {
        return aDominio(jpa.save(aFila(evidencia)));
    }

    @Override
    public Optional<Evidence> buscar(Long id) {
        return jpa.findById(id).map(EvidenciaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Evidence> todas() {
        return jpa.findAll().stream().map(EvidenciaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Evidence> deLasAlertas(Collection<Long> alertIds) {
        if (alertIds.isEmpty()) return List.of();
        return jpa.findByAlertIdInOrderByCreatedAtDesc(alertIds).stream().map(EvidenciaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Evidence> deLaAlerta(Long alertId) {
        return jpa.findByAlertIdOrderByCreatedAtAsc(alertId).stream().map(EvidenciaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Evidence> guardadasAntesDe(LocalDateTime fecha) {
        return jpa.findByCreatedAtBefore(fecha).stream().map(EvidenciaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public void eliminar(Evidence evidencia) {
        jpa.deleteById(evidencia.getId());
    }

    @Override
    public void eliminarTodas(List<Evidence> evidencias) {
        jpa.deleteAllById(evidencias.stream().map(Evidence::getId).toList());
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
