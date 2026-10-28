package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.UserReportRepository;
import com.alertamujer.admin.domain.model.UserReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: moderation cases in PostgreSQL. */
@Component
class UserReportPersistenceAdapter implements UserReportRepository {

    private final UserReportJpaRepository jpa;

    UserReportPersistenceAdapter(UserReportJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<UserReport> all() {
        return jpa.findAll().stream().map(UserReportPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<UserReport> withStatus(String status) {
        return jpa.findByStatus(status).stream().map(UserReportPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<UserReport> find(Long id) {
        return jpa.findById(id).map(UserReportPersistenceAdapter::toDomain);
    }

    @Override
    public List<UserReport> ofUser(Long userId) {
        return jpa.findByReportedUserId(userId).stream().map(UserReportPersistenceAdapter::toDomain).toList();
    }

    @Override
    public UserReport save(UserReport userReport) {
        return toDomain(jpa.save(toRow(userReport)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static UserReport toDomain(UserReportJpaEntity f) {
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

    static UserReportJpaEntity toRow(UserReport d) {
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
