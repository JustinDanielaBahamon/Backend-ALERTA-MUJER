package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lugar frecuente de la usuaria (casa, trabajo...) con su nivel de riesgo.
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

    public static final String RIESGO_POR_DEFECTO = "moderada";

    // ── Reglas ──────────────────────────────────────────────────────

    public static FrequentLocation nuevo(Long perfil, DatosLugar datos, LocalDateTime ahora) {
        FrequentLocation l = new FrequentLocation();
        l.userProfileId = perfil;
        l.name = datos.name();
        l.address = datos.address();
        l.city = datos.city();
        l.latitude = datos.latitude();
        l.longitude = datos.longitude();
        l.notes = datos.notes();
        l.riskLevel = datos.riskLevel() == null || datos.riskLevel().isBlank() ? RIESGO_POR_DEFECTO : datos.riskLevel();
        l.isActive = datos.isActive() == null || datos.isActive();
        l.createdAt = ahora;
        return l;
    }

    public boolean esDe(Long perfil) {
        return perfil != null && Objects.equals(userProfileId, perfil);
    }

    /** Actualización parcial: la web usa PATCH y el móvil envía solo lo modificado. */
    public void actualizar(DatosLugar datos, LocalDateTime ahora) {
        if (datos.name() != null && !datos.name().isBlank()) name = datos.name();
        if (datos.address() != null) address = datos.address();
        if (datos.city() != null) city = datos.city();
        if (datos.latitude() != null) latitude = datos.latitude();
        if (datos.longitude() != null) longitude = datos.longitude();
        if (datos.notes() != null) notes = datos.notes();
        if (datos.riskLevel() != null && !datos.riskLevel().isBlank()) riskLevel = datos.riskLevel();
        if (datos.isActive() != null) isActive = datos.isActive();
        updatedAt = ahora;
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
