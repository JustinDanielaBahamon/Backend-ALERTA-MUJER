package com.alertamujer.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila del perfil de una usuaria. El dominio es UserProfile. */
@Entity
@Table(name = "user_profile", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class UserProfileJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "tutorial_completed", nullable = false)
    private Boolean tutorialCompleted;

    @Column(name = "tutorial_seen_at")
    private LocalDateTime tutorialSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
