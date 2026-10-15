package com.alertamujer.admin.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Informe de alertas guardado por la administración (tabla admin.alert_report). */
@Entity
@Table(name = "alert_report", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AlertReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;

    /** pending, attended o closed. */
    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "user_label")
    private String userLabel;

    private String event;

    private String description;

    /** PDF o EXCEL: formato preferido al exportar. */
    @Column(nullable = false)
    private String format = "PDF";

    /** Ids de alertas separados por coma; vacío = usar los filtros. */
    @Column(name = "alert_ids")
    private String alertIds;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    /** Filtro opcional por estado de la alerta (active, resolved, cancelled, expired). */
    @Column(name = "alert_status")
    private String alertStatus;

    @Column(name = "export_count", nullable = false)
    private Integer exportCount = 0;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
