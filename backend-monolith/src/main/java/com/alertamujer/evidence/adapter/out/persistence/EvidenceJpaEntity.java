package com.alertamujer.evidence.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de la tabla de evidencias. Solo la usa el adaptador de persistencia; el dominio es Evidence. */
@Entity
@Table(name = "evidence", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class EvidenceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "sha256")
    private String sha256;

    @Column(name = "storage_key", unique = true)
    private String storageKey;

    @Column(name = "review_status", nullable = false)
    private String reviewStatus = "pending";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
