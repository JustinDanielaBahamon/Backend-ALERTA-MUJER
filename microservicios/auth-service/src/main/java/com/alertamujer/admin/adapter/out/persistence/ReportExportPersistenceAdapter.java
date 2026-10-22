package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.ReportExportRepository;
import com.alertamujer.admin.domain.model.AlertReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: alert reports in PostgreSQL. */
@Component
class ReportExportPersistenceAdapter implements ReportExportRepository {

    private final AlertReportJpaRepository jpa;

    ReportExportPersistenceAdapter(AlertReportJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<AlertReport> all() {
        return jpa.findAll().stream().map(ReportExportPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<AlertReport> find(Long id) {
        return jpa.findById(id).map(ReportExportPersistenceAdapter::toDomain);
    }

    @Override
    public AlertReport save(AlertReport report) {
        return toDomain(jpa.save(toRow(report)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static AlertReport toDomain(AlertReportJpaEntity f) {
        AlertReport d = new AlertReport();
        d.setId(f.getId());
        d.setName(f.getName());
        d.setCity(f.getCity());
        d.setStatus(f.getStatus());
        d.setUserLabel(f.getUserLabel());
        d.setEvent(f.getEvent());
        d.setDescription(f.getDescription());
        d.setFormat(f.getFormat());
        d.setAlertIds(f.getAlertIds());
        d.setFromDate(f.getFromDate());
        d.setToDate(f.getToDate());
        d.setAlertStatus(f.getAlertStatus());
        d.setExportCount(f.getExportCount());
        d.setCreatedByUserId(f.getCreatedByUserId());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static AlertReportJpaEntity toRow(AlertReport d) {
        AlertReportJpaEntity f = new AlertReportJpaEntity();
        f.setId(d.getId());
        f.setName(d.getName());
        f.setCity(d.getCity());
        f.setStatus(d.getStatus());
        f.setUserLabel(d.getUserLabel());
        f.setEvent(d.getEvent());
        f.setDescription(d.getDescription());
        f.setFormat(d.getFormat());
        f.setAlertIds(d.getAlertIds());
        f.setFromDate(d.getFromDate());
        f.setToDate(d.getToDate());
        f.setAlertStatus(d.getAlertStatus());
        f.setExportCount(d.getExportCount());
        f.setCreatedByUserId(d.getCreatedByUserId());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
