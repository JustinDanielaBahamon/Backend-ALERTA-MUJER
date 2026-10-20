package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.AccionModeracionRepositorio;
import com.alertamujer.admin.domain.model.ModerationAction;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/** Adaptador de salida: las acciones de moderación en PostgreSQL. */
@Component
class AccionModeracionPersistenceAdapter implements AccionModeracionRepositorio {

    private final ModerationActionJpaRepository jpa;

    AccionModeracionPersistenceAdapter(ModerationActionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ModerationAction> deCasos(Collection<Long> casoIds) {
        return jpa.findByUserReportIdIn(casoIds).stream().map(AccionModeracionPersistenceAdapter::aDominio).toList();
    }

    @Override
    public ModerationAction guardar(ModerationAction accion) {
        return aDominio(jpa.save(aFila(accion)));
    }

    static ModerationAction aDominio(ModerationActionJpaEntity f) {
        ModerationAction d = new ModerationAction();
        d.setId(f.getId());
        d.setUserReportId(f.getUserReportId());
        d.setAdminPerfilId(f.getAdminPerfilId());
        d.setActionType(f.getActionType());
        d.setNotes(f.getNotes());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static ModerationActionJpaEntity aFila(ModerationAction d) {
        ModerationActionJpaEntity f = new ModerationActionJpaEntity();
        f.setId(d.getId());
        f.setUserReportId(d.getUserReportId());
        f.setAdminPerfilId(d.getAdminPerfilId());
        f.setActionType(d.getActionType());
        f.setNotes(d.getNotes());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
