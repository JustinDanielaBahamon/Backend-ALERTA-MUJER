package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.RecuperacionRepositorio;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida: las solicitudes de recuperación en PostgreSQL. */
@Component
class RecuperacionPersistenceAdapter implements RecuperacionRepositorio {

    private final RecoveryRequestJpaRepository jpa;

    RecuperacionPersistenceAdapter(RecoveryRequestJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RecoveryRequest save(RecoveryRequest solicitud) {
        return aDominio(jpa.save(aFila(solicitud)));
    }

    @Override
    public void guardarTodas(List<RecoveryRequest> solicitudes) {
        jpa.saveAll(solicitudes.stream().map(RecuperacionPersistenceAdapter::aFila).toList());
    }

    @Override
    public List<RecoveryRequest> pendientesDeCuenta(Long accountId) {
        return jpa.findByAccountIdAndStatus(accountId, RecoveryRequest.PENDIENTE).stream()
                .map(RecuperacionPersistenceAdapter::aDominio).toList();
    }

    @Override
    public boolean existeHash(String hash) {
        return jpa.existsByTokenHash(hash);
    }

    static RecoveryRequest aDominio(RecoveryRequestJpaEntity f) {
        RecoveryRequest d = new RecoveryRequest();
        d.setId(f.getId());
        d.setAccountId(f.getAccountId());
        d.setMethod(f.getMethod());
        d.setTokenHash(f.getTokenHash());
        d.setExpiresAt(f.getExpiresAt());
        d.setUsedAt(f.getUsedAt());
        d.setStatus(f.getStatus());
        d.setAttempts(f.getAttempts());
        d.setRequestedIp(f.getRequestedIp());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static RecoveryRequestJpaEntity aFila(RecoveryRequest d) {
        RecoveryRequestJpaEntity f = new RecoveryRequestJpaEntity();
        f.setId(d.getId());
        f.setAccountId(d.getAccountId());
        f.setMethod(d.getMethod());
        f.setTokenHash(d.getTokenHash());
        f.setExpiresAt(d.getExpiresAt());
        f.setUsedAt(d.getUsedAt());
        f.setStatus(d.getStatus());
        f.setAttempts(d.getAttempts());
        f.setRequestedIp(d.getRequestedIp());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
