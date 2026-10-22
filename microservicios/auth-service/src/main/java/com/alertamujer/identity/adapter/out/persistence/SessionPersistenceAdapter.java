package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.SessionRepository;
import com.alertamujer.identity.domain.model.DeviceSession;
import org.springframework.stereotype.Component;

import java.util.List;

/** Output adapter: device sessions in PostgreSQL. */
@Component
class SessionPersistenceAdapter implements SessionRepository {

    private final DeviceSessionJpaRepository jpa;

    SessionPersistenceAdapter(DeviceSessionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<DeviceSession> activeForAccount(Long accountId) {
        return jpa.findByAccountIdAndStatus(accountId, DeviceSession.ACTIVE).stream().map(SessionPersistenceAdapter::toDomain).toList();
    }

    @Override
    public DeviceSession save(DeviceSession session) {
        return toDomain(jpa.save(toRow(session)));
    }

    static DeviceSession toDomain(DeviceSessionJpaEntity f) {
        DeviceSession d = new DeviceSession();
        d.setId(f.getId());
        d.setAccountId(f.getAccountId());
        d.setDeviceId(f.getDeviceId());
        d.setRefreshTokenHash(f.getRefreshTokenHash());
        d.setStatus(f.getStatus());
        d.setExpiresAt(f.getExpiresAt());
        d.setLastUsedAt(f.getLastUsedAt());
        d.setRevokedAt(f.getRevokedAt());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static DeviceSessionJpaEntity toRow(DeviceSession d) {
        DeviceSessionJpaEntity f = new DeviceSessionJpaEntity();
        f.setId(d.getId());
        f.setAccountId(d.getAccountId());
        f.setDeviceId(d.getDeviceId());
        f.setRefreshTokenHash(d.getRefreshTokenHash());
        f.setStatus(d.getStatus());
        f.setExpiresAt(d.getExpiresAt());
        f.setLastUsedAt(d.getLastUsedAt());
        f.setRevokedAt(d.getRevokedAt());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
