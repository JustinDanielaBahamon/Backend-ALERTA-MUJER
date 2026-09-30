package com.alertamujer.identity.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile", schema = "identity")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "tutorial_completed", nullable = false)
    private Boolean tutorialCompleted = false;

    @Column(name = "tutorial_seen_at")
    private LocalDateTime tutorialSeenAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public Boolean getTutorialCompleted() { return tutorialCompleted; }
    public void setTutorialCompleted(Boolean tutorialCompleted) { this.tutorialCompleted = tutorialCompleted; }
    public LocalDateTime getTutorialSeenAt() { return tutorialSeenAt; }
    public void setTutorialSeenAt(LocalDateTime tutorialSeenAt) { this.tutorialSeenAt = tutorialSeenAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
