package com.alertamujer.alerts.domain.model;

import com.alertamujer.shared.web.PhotoDataUri;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Emergency contact of a user: who gets notified when she activates an alert.
 */
public class EmergencyContact {

    private Long id;
    private Long userProfileId;
    private String contactName;
    private String telephone;
    private String email;
    private String relationship;
    /** Contact photo as a small image data URI (see PhotoDataUri). */
    private String photo;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // ── Reglas ──────────────────────────────────────────────────────

    public static EmergencyContact created(Long profile, ContactData data) {
        EmergencyContact c = new EmergencyContact();
        c.userProfileId = profile;
        c.contactName = data.contactName();
        c.telephone = data.telephone();
        c.email = data.email();
        c.relationship = data.relationship();
        c.photo = PhotoDataUri.validate(data.photo());
        return c;
    }

    public boolean esDe(Long profile) {
        return profile != null && Objects.equals(userProfileId, profile);
    }

    /** Only the sent fields change: a PUT with partial data must not erase the rest. */
    public void update(ContactData data) {
        if (data.contactName() != null) contactName = data.contactName();
        if (data.telephone() != null) telephone = data.telephone();
        if (data.email() != null) email = data.email().isBlank() ? null : data.email().trim();
        if (data.relationship() != null) relationship = data.relationship();
        // "" removes the photo; null leaves it as it was
        if (data.photo() != null) photo = PhotoDataUri.validate(data.photo());
    }

    /** Email without spaces ("" if none): without an email she cannot be notified. */
    public String emailToNotify() {
        return email == null ? "" : email.trim();
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
