package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.ModerationActionRepository;
import com.alertamujer.admin.domain.model.ModerationAction;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/** Adaptador de salida: las acciones de moderación en PostgreSQL. */
@Component
class ModerationActionPersistenceAdapter implements ModerationActionRepository {

    private final ModerationActionJpaRepository jpa;

    ModerationActionPersistenceAdapter(ModerationActionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ModerationAction> fromCases(Collection<Long> caseIds) {
        return jpa.findByUserReportIdIn(caseIds).stream().map(ModerationActionPersistenceAdapter::toDomain).toList();
    }

    @Override
    public ModerationAction save(ModerationAction action) {
        return toDomain(jpa.save(toRow(action)));
    }

    static ModerationAction toDomain(ModerationActionJpaEntity f) {
        ModerationAction d = new ModerationAction();
        d.setId(f.getId());
        d.setUserReportId(f.getUserReportId());
        d.setAdminProfileId(f.getAdminProfileId());
        d.setActionType(f.getActionType());
        d.setNotes(f.getNotes());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static ModerationActionJpaEntity toRow(ModerationAction d) {
        ModerationActionJpaEntity f = new ModerationActionJpaEntity();
        f.setId(d.getId());
        f.setUserReportId(d.getUserReportId());
        f.setAdminProfileId(d.getAdminProfileId());
        f.setActionType(d.getActionType());
        f.setNotes(d.getNotes());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
