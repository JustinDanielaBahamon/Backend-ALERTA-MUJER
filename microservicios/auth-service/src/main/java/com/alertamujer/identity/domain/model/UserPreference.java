package com.alertamujer.identity.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Preferencias de la usuaria (idioma y tema).
 */
@Entity
@Table(name = "user_preference", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @Column(name = "language", nullable = false)
    private String language = "es";

    @Column(name = "theme", nullable = false)
    private String theme = "light";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
