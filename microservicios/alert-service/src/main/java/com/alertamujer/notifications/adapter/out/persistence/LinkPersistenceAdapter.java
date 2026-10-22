package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.LinkRepository;
import com.alertamujer.notifications.domain.model.TrackingLink;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de salida: los enlaces de seguimiento en PostgreSQL. */
@Component
class LinkPersistenceAdapter implements LinkRepository {

    private final TrackingLinkJpaRepository jpa;

    LinkPersistenceAdapter(TrackingLinkJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TrackingLink save(TrackingLink link) {
        return toDomain(jpa.save(toRow(link)));
    }

    @Override
    public Optional<TrackingLink> byHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(LinkPersistenceAdapter::toDomain);
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
