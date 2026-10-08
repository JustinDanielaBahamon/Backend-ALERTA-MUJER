package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "frequent_location", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FrequentLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "name", nullable = false)
    private String name;

    private String address;
    private String city;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    private String notes;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel = "moderada";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}