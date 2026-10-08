package com.alertamujer.identity.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * UserProfile entity following database schema: Biometrics and preferences.
 * Personal data (name, contact info) are in User table.
 */
@Entity
@Table(name = "user_profile", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_user_profile_user"))
    @ToString.Exclude
    private User user;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Long userId;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "tutorial_completed", nullable = false)
    private Boolean tutorialCompleted = false;

    @Column(name = "tutorial_seen_at")
    private LocalDateTime tutorialSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
