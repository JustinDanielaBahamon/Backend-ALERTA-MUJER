package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.SolicitudEliminacionRepositorio;
import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: las solicitudes de eliminación en PostgreSQL. */
@Component
class SolicitudEliminacionPersistenceAdapter implements SolicitudEliminacionRepositorio {

    private final AccountDeletionRequestJpaRepository jpa;

    SolicitudEliminacionPersistenceAdapter(AccountDeletionRequestJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<AccountDeletionRequest> todas() {
        return jpa.findAllByOrderByCreatedAtDesc().stream().map(SolicitudEliminacionPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<AccountDeletionRequest> find(Long id) {
        return jpa.findById(id).map(SolicitudEliminacionPersistenceAdapter::aDominio);
    }

    @Override
    public boolean hayPendiente(Long userId) {
        return jpa.existsByUserIdAndStatus(userId, AccountDeletionRequest.PENDIENTE);
    }

    @Override
    public AccountDeletionRequest save(AccountDeletionRequest solicitud) {
        return aDominio(jpa.save(aFila(solicitud)));
    }

    static AccountDeletionRequest aDominio(AccountDeletionRequestJpaEntity f) {
        AccountDeletionRequest d = new AccountDeletionRequest();
        d.setId(f.getId());
        d.setUserId(f.getUserId());
        d.setRequestedBy(f.getRequestedBy());
        d.setReason(f.getReason());
        d.setStatus(f.getStatus());
        d.setDecidedBy(f.getDecidedBy());
        d.setDecisionNote(f.getDecisionNote());
        d.setCreatedAt(f.getCreatedAt());
        d.setDecidedAt(f.getDecidedAt());
        return d;
    }

    static AccountDeletionRequestJpaEntity aFila(AccountDeletionRequest d) {
        AccountDeletionRequestJpaEntity f = new AccountDeletionRequestJpaEntity();
        f.setId(d.getId());
        f.setUserId(d.getUserId());
        f.setRequestedBy(d.getRequestedBy());
        f.setReason(d.getReason());
        f.setStatus(d.getStatus());
        f.setDecidedBy(d.getDecidedBy());
        f.setDecisionNote(d.getDecisionNote());
        f.setCreatedAt(d.getCreatedAt());
        f.setDecidedAt(d.getDecidedAt());
        return f;
    }
}
