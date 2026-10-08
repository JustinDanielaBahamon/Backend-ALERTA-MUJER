package com.alertamujer.resources.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "emergency_resource", schema = "resource")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmergencyResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    private String telephone;
    @Column(name = "secondary_telephone")
    private String secondaryTelephone;
    private String email;
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}