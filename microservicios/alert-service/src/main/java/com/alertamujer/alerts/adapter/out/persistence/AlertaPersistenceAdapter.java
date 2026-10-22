package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: guarda las alertas en PostgreSQL con JPA y traduce fila ↔ dominio. */
@Component
class AlertaPersistenceAdapter implements AlertaRepositorio {

    private final AlertJpaRepository jpa;

    AlertaPersistenceAdapter(AlertJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Alert save(Alert alert) {
        return toDomain(jpa.save(toRow(alert)));
    }

    @Override
    public List<Alert> saveAll(List<Alert> alerts) {
        return jpa.saveAll(alerts.stream().map(AlertaPersistenceAdapter::toRow).toList()).stream()
                .map(AlertaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<Alert> find(Long id) {
        return jpa.findById(id).map(AlertaPersistenceAdapter::toDomain);
    }

    @Override
    public List<Alert> findMany(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(AlertaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Alert> all() {
        return jpa.findAll().stream().map(AlertaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Alert> ofUser(Long profile) {
        return jpa.findByUserProfileId(profile).stream().map(AlertaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Alert> active() {
        return jpa.findByStatus(Alert.ACTIVE).stream().map(AlertaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<Alert> enCurso(Long profile) {
        return jpa.findFirstByUserProfileIdAndStatusOrderByStartedAtDesc(profile, Alert.ACTIVE).map(AlertaPersistenceAdapter::toDomain);
    }

    @Override
    public long kept(Long profile) {
        return jpa.countByUserProfileIdAndKeepEvidenceTrue(profile);
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static Alert toDomain(AlertJpaEntity f) {
        Alert d = new Alert();
        d.setId(f.getId());
        d.setUserProfileId(f.getUserProfileId());
        d.setDeviceId(f.getDeviceId());
        d.setAlertType(f.getAlertType());
        d.setActivationMethod(f.getActivationMethod());
        d.setStatus(f.getStatus());
        d.setMessage(f.getMessage());
        d.setLatitude(f.getLatitude());
        d.setLongitude(f.getLongitude());
        d.setStartedAt(f.getStartedAt());
        d.setEndedAt(f.getEndedAt());
        d.setCancelledAt(f.getCancelledAt());
        d.setCreatedAt(f.getCreatedAt());
        d.setKeepEvidence(f.isKeepEvidence());
        return d;
    }

    static AlertJpaEntity toRow(Alert d) {
        AlertJpaEntity f = new AlertJpaEntity();
        f.setId(d.getId());
        f.setUserProfileId(d.getUserProfileId());
        f.setDeviceId(d.getDeviceId());
        f.setAlertType(d.getAlertType());
        f.setActivationMethod(d.getActivationMethod());
        f.setStatus(d.getStatus());
        f.setMessage(d.getMessage());
        f.setLatitude(d.getLatitude());
        f.setLongitude(d.getLongitude());
        f.setStartedAt(d.getStartedAt());
        f.setEndedAt(d.getEndedAt());
        f.setCancelledAt(d.getCancelledAt());
        f.setCreatedAt(d.getCreatedAt());
        f.setKeepEvidence(d.isKeepEvidence());
        return f;
    }
}
