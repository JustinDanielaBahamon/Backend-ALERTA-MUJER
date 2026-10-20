package com.alertamujer.alerts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de un contacto de emergencia. El dominio es EmergencyContact. */
@Entity
@Table(name = "emergency_contact", schema = "alert")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class EmergencyContactJpaEntity {
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

    @Column(columnDefinition = "text")
    private String photo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
