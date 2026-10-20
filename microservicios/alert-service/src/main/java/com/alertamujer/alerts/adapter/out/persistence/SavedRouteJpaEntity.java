package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.domain.model.RoutePoint;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Fila de un recorrido guardado (los puntos van en JSONB). El dominio es SavedRoute. */
@Entity
@Table(name = "saved_route", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class SavedRouteJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_label")
    private String startLabel;

    @Column(name = "end_label")
    private String endLabel;

    private String city;

    private String department;

    private String country;

    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "points", nullable = false, columnDefinition = "jsonb")
    private List<RoutePoint> points;

    @Column(name = "is_starred", nullable = false)
    private Boolean isStarred;

    @Column(name = "is_manual", nullable = false)
    private Boolean isManual;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
