package com.alertamujer.zones.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Zona del mapa (de riesgo, segura, de ayuda, policía o asistencia) que publica la administración.
 * Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class Zone {

    private Long id;
    private Integer createdByAdminId;
    private String name;
    /** risk | safe | help | policia | asistencia */
    private String zoneType;
    /** low | medium | high */
    private String riskLevel = "medium";
    private String description;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal radiusMeters;
    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // ── Reglas ──────────────────────────────────────────────────────

    public static Zone newOne(ZoneData d) {
        Zone z = new Zone();
        z.createdByAdminId = d.createdByAdminId();
        z.replace(d);
        if (d.riskLevel() == null) z.riskLevel = "medium";
        if (d.isActive() == null) z.isActive = true;
        return z;
    }

    /** Edición desde el panel: reemplaza todos los datos de la zona. */
    public void replace(ZoneData d) {
        name = d.name();
        zoneType = d.zoneType();
        riskLevel = d.riskLevel();
        description = d.description();
        address = d.address();
        city = d.city();
        latitude = d.latitude();
        longitude = d.longitude();
        radiusMeters = d.radiusMeters();
        isActive = d.isActive();
    }

    public boolean active() {
        return Boolean.TRUE.equals(isActive);
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getCreatedByAdminId() { return createdByAdminId; }
    public void setCreatedByAdminId(Integer createdByAdminId) { this.createdByAdminId = createdByAdminId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(BigDecimal radiusMeters) { this.radiusMeters = radiusMeters; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
