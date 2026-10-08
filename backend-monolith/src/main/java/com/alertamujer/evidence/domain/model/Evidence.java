package com.alertamujer.evidence.domain.model;

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

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}