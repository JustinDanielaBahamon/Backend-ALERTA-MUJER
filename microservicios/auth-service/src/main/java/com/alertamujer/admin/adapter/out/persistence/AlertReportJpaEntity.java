package com.alertamujer.admin.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Row of an alert report. The domain is AlertReport. */
@Entity
@Table(name = "alert_report", schema = "admin")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class AlertReportJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;

    @Column(nullable = false)
    private String status;

    @Column(name = "user_label")
    private String userLabel;

    private String event;

    private String description;

    @Column(nullable = false)
    private String format;

    @Column(name = "alert_ids")
    private String alertIds;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    @Column(name = "alert_status")
    private String alertStatus;

    @Column(name = "export_count", nullable = false)
    private Integer exportCount;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
