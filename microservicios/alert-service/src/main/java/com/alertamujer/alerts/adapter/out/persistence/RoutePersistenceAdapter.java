package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.RouteRepository;
import com.alertamujer.alerts.domain.model.SavedRoute;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: saved routes in PostgreSQL. */
@Component
class RoutePersistenceAdapter implements RouteRepository {

    private final SavedRouteJpaRepository jpa;

    RoutePersistenceAdapter(SavedRouteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<SavedRoute> ofUser(Long profile) {
        return jpa.findByUserProfileIdOrderByCreatedAtDesc(profile).stream().map(RoutePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<SavedRoute> find(Long id) {
        return jpa.findById(id).map(RoutePersistenceAdapter::toDomain);
    }

    @Override
    public long count(Long profile) {
        return jpa.countByUserProfileId(profile);
    }

    @Override
    public long countStarred(Long profile) {
        return jpa.countByUserProfileIdAndIsStarredTrue(profile);
    }

    @Override
    public SavedRoute save(SavedRoute route) {
        return toDomain(jpa.save(toRow(route)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static SavedRoute toDomain(SavedRouteJpaEntity f) {
        SavedRoute d = new SavedRoute();
        d.setId(f.getId());
        d.setUserProfileId(f.getUserProfileId());
        d.setName(f.getName());
        d.setStartLabel(f.getStartLabel());
        d.setEndLabel(f.getEndLabel());
        d.setCity(f.getCity());
        d.setDepartment(f.getDepartment());
        d.setCountry(f.getCountry());
        d.setDistanceKm(f.getDistanceKm());
        d.setEstimatedMinutes(f.getEstimatedMinutes());
        d.setPoints(f.getPoints());
        d.setIsStarred(f.getIsStarred());
        d.setIsManual(f.getIsManual());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static SavedRouteJpaEntity toRow(SavedRoute d) {
        SavedRouteJpaEntity f = new SavedRouteJpaEntity();
        f.setId(d.getId());
        f.setUserProfileId(d.getUserProfileId());
        f.setName(d.getName());
        f.setStartLabel(d.getStartLabel());
        f.setEndLabel(d.getEndLabel());
        f.setCity(d.getCity());
        f.setDepartment(d.getDepartment());
        f.setCountry(d.getCountry());
        f.setDistanceKm(d.getDistanceKm());
        f.setEstimatedMinutes(d.getEstimatedMinutes());
        f.setPoints(d.getPoints());
        f.setIsStarred(d.getIsStarred());
        f.setIsManual(d.getIsManual());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
