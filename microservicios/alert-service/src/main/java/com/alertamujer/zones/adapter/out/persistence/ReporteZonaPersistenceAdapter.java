package com.alertamujer.zones.adapter.out.persistence;

import com.alertamujer.zones.application.port.out.ReporteZonaRepositorio;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los reportes de zonas en PostgreSQL. */
@Component
class ReporteZonaPersistenceAdapter implements ReporteZonaRepositorio {

    private final ZoneReportJpaRepository jpa;

    ReporteZonaPersistenceAdapter(ZoneReportJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ZoneReport> all() {
        return jpa.findAll().stream().map(ReporteZonaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<ZoneReport> find(Long id) {
        return jpa.findById(id).map(ReporteZonaPersistenceAdapter::toDomain);
    }

    @Override
    public List<ZoneReport> deZona(Long zoneId) {
        return jpa.findByZoneId(zoneId).stream().map(ReporteZonaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ZoneReport> dePerfil(Long profile) {
        return jpa.findByUserProfileId(profile).stream().map(ReporteZonaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ZoneReport> withStatus(String status) {
        return jpa.findByStatus(status).stream().map(ReporteZonaPersistenceAdapter::toDomain).toList();
    }

    @Override
    public ZoneReport save(ZoneReport userReport) {
        return toDomain(jpa.save(toRow(userReport)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static ZoneReport toDomain(ZoneReportJpaEntity f) {
        ZoneReport d = new ZoneReport();
        d.setId(f.getId());
        d.setZoneId(f.getZoneId());
        d.setUserProfileId(f.getUserProfileId());
        d.setClassification(f.getClassification());
        d.setComment(f.getComment());
        d.setStatus(f.getStatus());
        d.setReviewedByAdminId(f.getReviewedByAdminId());
        d.setReviewedAt(f.getReviewedAt());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static ZoneReportJpaEntity toRow(ZoneReport d) {
        ZoneReportJpaEntity f = new ZoneReportJpaEntity();
        f.setId(d.getId());
        f.setZoneId(d.getZoneId());
        f.setUserProfileId(d.getUserProfileId());
        f.setClassification(d.getClassification());
        f.setComment(d.getComment());
        f.setStatus(d.getStatus());
        f.setReviewedByAdminId(d.getReviewedByAdminId());
        f.setReviewedAt(d.getReviewedAt());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}
