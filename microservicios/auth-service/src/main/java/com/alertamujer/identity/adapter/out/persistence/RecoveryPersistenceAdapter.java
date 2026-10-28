package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.RecoveryRepository;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/** Output adapter: recovery requests in PostgreSQL. */
@Component
class RecoveryPersistenceAdapter implements RecoveryRepository {

    private final RecoveryRequestJpaRepository jpa;

    RecoveryPersistenceAdapter(RecoveryRequestJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RecoveryRequest save(RecoveryRequest deletionRequest) {
        return toDomain(jpa.save(toRow(deletionRequest)));
    }

    @Override
    public void saveAll(List<RecoveryRequest> deletionRequests) {
        jpa.saveAll(deletionRequests.stream().map(RecoveryPersistenceAdapter::toRow).toList());
    }

    @Override
    public List<RecoveryRequest> pendingForAccount(Long accountId) {
        return jpa.findByAccountIdAndStatus(accountId, RecoveryRequest.PENDING).stream()
                .map(RecoveryPersistenceAdapter::toDomain).toList();
    }

    @Override
    public boolean hashExists(String hash) {
        return jpa.existsByTokenHash(hash);
    }

    static RecoveryRequest toDomain(RecoveryRequestJpaEntity f) {
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

    static RecoveryRequestJpaEntity toRow(RecoveryRequest d) {
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
