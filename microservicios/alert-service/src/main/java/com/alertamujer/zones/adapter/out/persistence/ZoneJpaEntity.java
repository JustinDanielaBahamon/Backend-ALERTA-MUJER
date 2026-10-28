package com.alertamujer.zones.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Row of a zone. The domain is Zone. */
@Entity
@Table(name = "zone", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ZoneJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_by_admin_id")
    private Integer createdByAdminId;

    @Column(nullable = false)
    private String name;

    @Column(name = "zone_type", nullable = false)
    private String zoneType;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    private String description;

    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "radius_meters", nullable = false, precision = 8, scale = 2)
    private BigDecimal radiusMeters;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
