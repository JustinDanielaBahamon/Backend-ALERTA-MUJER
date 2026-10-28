package com.alertamujer.resources.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Help resource (emergency line, police station, hospital, organization...)
 * published by the administration. Domain model: knows neither the database nor Spring.
 */
public class EmergencyResource {

    private Long id;
    private String name;
    private String resourceType;
    private String telephone;
    private String secondaryTelephone;
    private String email;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // ── Reglas ──────────────────────────────────────────────────────

    public static EmergencyResource created(ResourceData d) {
        EmergencyResource r = new EmergencyResource();
        r.replace(d);
        if (d.isActive() == null) r.isActive = true;
        return r;
    }

    /** Edit from the panel: replaces all the resource data. */
    public void replace(ResourceData d) {
        name = d.name();
        resourceType = d.resourceType();
        telephone = d.telephone();
        secondaryTelephone = d.secondaryTelephone();
        email = d.email();
        address = d.address();
        city = d.city();
        latitude = d.latitude();
        longitude = d.longitude();
        description = d.description();
        isActive = d.isActive();
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getSecondaryTelephone() { return secondaryTelephone; }
    public void setSecondaryTelephone(String secondaryTelephone) { this.secondaryTelephone = secondaryTelephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
