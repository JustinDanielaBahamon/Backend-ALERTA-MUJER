package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.AuditRepository;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.AuditFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Output adapter: the audit in PostgreSQL, with the filter translated to a JPA query. */
@Component
class AuditPersistenceAdapter implements AuditRepository {

    private final AuditLogJpaRepository jpa;

    AuditPersistenceAdapter(AuditLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    @Override
    public AuditLogPage page(AuditFilter filter, int page, int size) {
        Page<AuditLogJpaEntity> p = jpa.findAll(specification(filter), PageRequest.of(page, size, NEWEST_FIRST));
        return new AuditLogPage(p.getContent().stream().map(AuditPersistenceAdapter::toDomain).toList(), p.getTotalElements(), p.getNumber());
    }

    @Override
    public List<AuditLog> first(AuditFilter filter, int max) {
        return jpa.findAll(specification(filter), PageRequest.of(0, max, NEWEST_FIRST)).getContent().stream()
                .map(AuditPersistenceAdapter::toDomain).toList();
    }

    @Override
    public AuditLog save(AuditLog record) {
        return toDomain(jpa.save(toRow(record)));
    }

    static Specification<AuditLogJpaEntity> specification(AuditFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.actorAccountId() != null) {
                p.add(f.actorAccountId() == 0 ? cb.isNull(root.get("accountId")) : cb.equal(root.get("accountId"), f.actorAccountId()));
            }
            if (f.entityType() != null && !f.entityType().isBlank()) p.add(cb.equal(root.get("entityType"), f.entityType().trim().toUpperCase()));
            if (f.action() != null && !f.action().isBlank()) p.add(cb.equal(root.get("action"), f.action().trim().toUpperCase()));
            if (f.from() != null) p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.from().atStartOfDay()));
            if (f.to() != null) p.add(cb.lessThan(root.get("createdAt"), f.to().plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    static AuditLog toDomain(AuditLogJpaEntity f) {
        AuditLog d = new AuditLog();
        d.setId(f.getId());
        d.setAccountId(f.getAccountId());
        d.setAction(f.getAction());
        d.setEntityType(f.getEntityType());
        d.setEntityId(f.getEntityId());
        d.setDescription(f.getDescription());
        d.setIpAddress(f.getIpAddress());
        d.setSource(f.getSource());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static AuditLogJpaEntity toRow(AuditLog d) {
        AuditLogJpaEntity f = new AuditLogJpaEntity();
        f.setId(d.getId());
        f.setAccountId(d.getAccountId());
        f.setAction(d.getAction());
        f.setEntityType(d.getEntityType());
        f.setEntityId(d.getEntityId());
        f.setDescription(d.getDescription());
        f.setIpAddress(d.getIpAddress());
        f.setSource(d.getSource());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}
