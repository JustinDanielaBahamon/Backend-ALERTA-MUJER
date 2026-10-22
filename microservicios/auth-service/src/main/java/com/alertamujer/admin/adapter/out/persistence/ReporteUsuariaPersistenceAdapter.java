package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.ReporteUsuariaRepositorio;
import com.alertamujer.admin.domain.model.UserReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los casos de moderación en PostgreSQL. */
@Component
class ReporteUsuariaPersistenceAdapter implements ReporteUsuariaRepositorio {

    private final UserReportJpaRepository jpa;

    ReporteUsuariaPersistenceAdapter(UserReportJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<UserReport> todos() {
        return jpa.findAll().stream().map(ReporteUsuariaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<UserReport> conEstado(String status) {
        return jpa.findByStatus(status).stream().map(ReporteUsuariaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<UserReport> find(Long id) {
        return jpa.findById(id).map(ReporteUsuariaPersistenceAdapter::aDominio);
    }

    @Override
    public List<UserReport> deUsuaria(Long userId) {
        return jpa.findByReportedUserId(userId).stream().map(ReporteUsuariaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public UserReport save(UserReport reporte) {
        return aDominio(jpa.save(aFila(reporte)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static UserReport aDominio(UserReportJpaEntity f) {
        UserReport d = new UserReport();
        d.setId(f.getId());
        d.setReportedUserId(f.getReportedUserId());
        d.setReporterUserId(f.getReporterUserId());
        d.setReason(f.getReason());
        d.setDescription(f.getDescription());
        d.setSeverity(f.getSeverity());
        d.setStatus(f.getStatus());
        d.setReviewedByAdminId(f.getReviewedByAdminId());
        d.setReviewedAt(f.getReviewedAt());
        d.setResolutionNotes(f.getResolutionNotes());
        d.setEvidence(f.getEvidence());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static UserReportJpaEntity aFila(UserReport d) {
        UserReportJpaEntity f = new UserReportJpaEntity();
        f.setId(d.getId());
        f.setReportedUserId(d.getReportedUserId());
        f.setReporterUserId(d.getReporterUserId());
        f.setReason(d.getReason());
        f.setDescription(d.getDescription());
        f.setSeverity(d.getSeverity());
        f.setStatus(d.getStatus());
        f.setReviewedByAdminId(d.getReviewedByAdminId());
        f.setReviewedAt(d.getReviewedAt());
        f.setResolutionNotes(d.getResolutionNotes());
        f.setEvidence(d.getEvidence());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
