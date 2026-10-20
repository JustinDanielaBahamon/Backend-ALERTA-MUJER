package com.alertamujer.identity.domain.model;

import java.time.LocalDateTime;

/**
 * Perfil de la usuaria: a él pertenecen sus alertas, contactos y ubicaciones; guarda la foto.
 */
public class UserProfile {

    private Long id;
    private Long userId;
    /** Foto de perfil como data URI (ver FotoDataUri) o null. */
    private String profilePhotoUrl;
    private Boolean tutorialCompleted = false;
    private LocalDateTime tutorialSeenAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // ── Reglas ──────────────────────────────────────────────────────

    public static UserProfile de(Long userId) {
        UserProfile p = new UserProfile();
        p.userId = userId;
        return p;
    }

    // ── Datos ───────────────────────────────────────────────────────

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
