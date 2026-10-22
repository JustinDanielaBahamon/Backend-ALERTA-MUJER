package com.alertamujer.admin.adapter.out.persistence;

import com.alertamujer.admin.application.port.out.AuditoriaRepositorio;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.FiltroAuditoria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Adaptador de salida: la auditoría en PostgreSQL, con el filtro traducido a una consulta de JPA. */
@Component
class AuditoriaPersistenceAdapter implements AuditoriaRepositorio {

    private final AuditLogJpaRepository jpa;

    AuditoriaPersistenceAdapter(AuditLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    private static final Sort MAS_RECIENTES = Sort.by(Sort.Direction.DESC, "id");

    @Override
    public Pagina pagina(FiltroAuditoria filtro, int pagina, int size) {
        Page<AuditLogJpaEntity> p = jpa.findAll(especificacion(filtro), PageRequest.of(pagina, size, MAS_RECIENTES));
        return new Pagina(p.getContent().stream().map(AuditoriaPersistenceAdapter::aDominio).toList(), p.getTotalElements(), p.getNumber());
    }

    @Override
    public List<AuditLog> primeros(FiltroAuditoria filtro, int maximo) {
        return jpa.findAll(especificacion(filtro), PageRequest.of(0, maximo, MAS_RECIENTES)).getContent().stream()
                .map(AuditoriaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public AuditLog save(AuditLog registro) {
        return aDominio(jpa.save(aFila(registro)));
    }

    static Specification<AuditLogJpaEntity> especificacion(FiltroAuditoria f) {
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

    static AuditLog aDominio(AuditLogJpaEntity f) {
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

    static AuditLogJpaEntity aFila(AuditLog d) {
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
