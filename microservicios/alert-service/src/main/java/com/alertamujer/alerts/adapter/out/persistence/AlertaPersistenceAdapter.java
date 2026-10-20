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
    public Alert guardar(Alert alerta) {
        return aDominio(jpa.save(aFila(alerta)));
    }

    @Override
    public List<Alert> guardarTodas(List<Alert> alertas) {
        return jpa.saveAll(alertas.stream().map(AlertaPersistenceAdapter::aFila).toList()).stream()
                .map(AlertaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<Alert> buscar(Long id) {
        return jpa.findById(id).map(AlertaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Alert> buscarVarias(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(AlertaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Alert> todas() {
        return jpa.findAll().stream().map(AlertaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Alert> deLaUsuaria(Long perfil) {
        return jpa.findByUserProfileId(perfil).stream().map(AlertaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Alert> activas() {
        return jpa.findByStatus(Alert.ACTIVA).stream().map(AlertaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<Alert> enCurso(Long perfil) {
        return jpa.findFirstByUserProfileIdAndStatusOrderByStartedAtDesc(perfil, Alert.ACTIVA).map(AlertaPersistenceAdapter::aDominio);
    }

    @Override
    public long conservadas(Long perfil) {
        return jpa.countByUserProfileIdAndKeepEvidenceTrue(perfil);
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    static Alert aDominio(AlertJpaEntity f) {
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

    static AlertJpaEntity aFila(Alert d) {
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
