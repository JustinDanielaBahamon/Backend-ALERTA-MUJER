package com.alertamujer.alerts.domain.model;

import com.alertamujer.shared.exception.ConflictException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Recorrido guardado por la usuaria (máximo 4 por usuaria, máximo 2 estrella).
 * Compartido entre la app móvil y la web.
 */
public class SavedRoute {

    private Long id;
    private Long userProfileId;
    private String name;
    private String startLabel;
    private String endLabel;
    private String city;
    private String department;
    private String country;
    private BigDecimal distanceKm;
    private Integer estimatedMinutes;
    private List<RoutePoint> points = new ArrayList<>();
    private Boolean isStarred = false;
    private Boolean isManual = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // Reglas compartidas con web y móvil (también las garantiza un trigger en la base)
    public static final int MAX_ROUTES = 4;
    public static final int MAX_STARRED = 2;

    // ── Reglas ──────────────────────────────────────────────────────

    /** Nombre y al menos un punto de inicio y uno de fin, con coordenadas válidas. */
    public static void validate(String name, List<RoutePoint> puntos) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El recorrido necesita un nombre");
        }
        if (puntos == null || puntos.size() < 2) {
            throw new IllegalArgumentException("El recorrido necesita al menos un punto de inicio y uno de fin");
        }
        for (RoutePoint p : puntos) {
            if (p == null || p.latitude() == null || p.longitude() == null
                    || Math.abs(p.latitude()) > 90 || Math.abs(p.longitude()) > 180) {
                throw new IllegalArgumentException("Coordenadas inválidas en el recorrido");
            }
        }
    }

    /** recorridos y estrellas = los que ya tiene la usuaria. */
    public static void verificarCupo(long recorridos) {
        if (recorridos >= MAX_ROUTES) {
            throw new ConflictException("Solo puedes tener " + MAX_ROUTES + " recorridos guardados. Elimina uno para crear otro.");
        }
    }

    /** Igual que en el móvil: un recorrido nuevo es estrella mientras haya cupo. */
    public void estrellaInicial(long estrellas) {
        isStarred = estrellas < MAX_STARRED;
    }

    public boolean esDe(Long profile) {
        return profile != null && Objects.equals(userProfileId, profile);
    }

    public void rename(String name) {
        if (name != null && !name.isBlank()) this.name = name.trim();
    }

    public void marcarEstrella(Boolean estrella, long estrellas) {
        if (estrella == null || estrella.equals(isStarred)) return;
        if (estrella && estrellas >= MAX_STARRED) {
            throw new ConflictException("Solo puedes tener " + MAX_STARRED + " recorridos estrella. Quita la estrella a otro para marcar este.");
        }
        isStarred = estrella;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserProfileId() { return userProfileId; }
    public void setUserProfileId(Long userProfileId) { this.userProfileId = userProfileId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStartLabel() { return startLabel; }
    public void setStartLabel(String startLabel) { this.startLabel = startLabel; }
    public String getEndLabel() { return endLabel; }
    public void setEndLabel(String endLabel) { this.endLabel = endLabel; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public List<RoutePoint> getPoints() { return points; }
    public void setPoints(List<RoutePoint> points) { this.points = points; }
    public Boolean getIsStarred() { return isStarred; }
    public void setIsStarred(Boolean isStarred) { this.isStarred = isStarred; }
    public Boolean getIsManual() { return isManual; }
    public void setIsManual(Boolean isManual) { this.isManual = isManual; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
