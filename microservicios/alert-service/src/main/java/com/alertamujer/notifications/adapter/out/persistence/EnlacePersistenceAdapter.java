package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.EnlaceRepositorio;
import com.alertamujer.notifications.domain.model.TrackingLink;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de salida: los enlaces de seguimiento en PostgreSQL. */
@Component
class EnlacePersistenceAdapter implements EnlaceRepositorio {

    private final TrackingLinkJpaRepository jpa;

    EnlacePersistenceAdapter(TrackingLinkJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TrackingLink save(TrackingLink enlace) {
        return toDomain(jpa.save(toRow(enlace)));
    }

    @Override
    public Optional<TrackingLink> porHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(EnlacePersistenceAdapter::toDomain);
    }

    static TrackingLink toDomain(TrackingLinkJpaEntity f) {
        TrackingLink d = new TrackingLink();
        d.setId(f.getId());
        d.setAlertId(f.getAlertId());
        d.setTokenHash(f.getTokenHash());
        d.setCreatedAt(f.getCreatedAt());
        d.setRevokedAt(f.getRevokedAt());
        return d;
    }

    static TrackingLinkJpaEntity toRow(TrackingLink d) {
        TrackingLinkJpaEntity f = new TrackingLinkJpaEntity();
        f.setId(d.getId());
        f.setAlertId(d.getAlertId());
        f.setTokenHash(d.getTokenHash());
        f.setCreatedAt(d.getCreatedAt());
        f.setRevokedAt(d.getRevokedAt());
        return f;
    }
}
