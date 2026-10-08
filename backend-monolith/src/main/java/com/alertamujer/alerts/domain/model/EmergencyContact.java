package com.alertamujer.alerts.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * EmergencyContact entity following database schema: Contact information.
 * References user_profile_id as foreign key.
 */
@Entity
@Table(name = "emergency_contact", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    private String telephone;
    private String email;
    private String relationship;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
