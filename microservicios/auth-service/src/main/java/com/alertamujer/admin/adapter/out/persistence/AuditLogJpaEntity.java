package com.alertamujer.admin.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Audit row (written by the triggers and the session events). The domain is AuditLog. */
@Entity
@Table(name = "audit_log", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class AuditLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
