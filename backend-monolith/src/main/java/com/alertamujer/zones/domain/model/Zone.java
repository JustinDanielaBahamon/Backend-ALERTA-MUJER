package com.alertamujer.zones.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Zone entity following 3NF.
 * NOTE: 'city' field violates 3NF - should reference a City table in future refactoring.
 * Currently kept as string due to existing schema constraints.
 */
@Entity
@Table(name = "zone", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Zone {
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
    private String riskLevel = "medium";

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
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}