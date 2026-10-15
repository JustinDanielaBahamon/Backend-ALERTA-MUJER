package com.alertamujer.evidence.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "evidence", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Evidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    /** photo | video | audio | document */
    @Column(name = "media_type", nullable = false)
    private String mediaType;

    /** Ruta de la API para obtener el archivo (nunca una URL pública). */
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    /** Huella del archivo para demostrar que no fue alterado. */
    @Column(name = "sha256")
    private String sha256;

    /** Nombre del archivo en el almacenamiento. Interno: no se expone. */
    @JsonIgnore
    @Column(name = "storage_key", unique = true)
    private String storageKey;

    /** Revisión de la administración: pending, verified o rejected. */
    @Column(name = "review_status", nullable = false)
    private String reviewStatus = "pending";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
