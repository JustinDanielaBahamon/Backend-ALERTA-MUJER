package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The user's frequent place (home, work...) with its risk level.
 */
public class FrequentLocation {

    private Long id;
    private Long userProfileId;
    private String name;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String notes;
    private String riskLevel;
    private Boolean isActive;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public static final String DEFAULT_RISK = "moderada";

    // ── Reglas ──────────────────────────────────────────────────────

    public static FrequentLocation created(Long profile, PlaceData data, LocalDateTime now) {
        FrequentLocation l = new FrequentLocation();
        l.userProfileId = profile;
        l.name = data.name();
        l.address = data.address();
        l.city = data.city();
        l.latitude = data.latitude();
        l.longitude = data.longitude();
        l.notes = data.notes();
        l.riskLevel = data.riskLevel() == null || data.riskLevel().isBlank() ? DEFAULT_RISK : data.riskLevel();
        l.isActive = data.isActive() == null || data.isActive();
        l.createdAt = now;
        return l;
    }

    public boolean esDe(Long profile) {
        return profile != null && Objects.equals(userProfileId, profile);
    }

    /** Partial update: the web uses PATCH and mobile sends only what changed. */
    public void update(PlaceData data, LocalDateTime now) {
        if (data.name() != null && !data.name().isBlank()) name = data.name();
        if (data.address() != null) address = data.address();
        if (data.city() != null) city = data.city();
        if (data.latitude() != null) latitude = data.latitude();
        if (data.longitude() != null) longitude = data.longitude();
        if (data.notes() != null) notes = data.notes();
        if (data.riskLevel() != null && !data.riskLevel().isBlank()) riskLevel = data.riskLevel();
        if (data.isActive() != null) isActive = data.isActive();
        updatedAt = now;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
