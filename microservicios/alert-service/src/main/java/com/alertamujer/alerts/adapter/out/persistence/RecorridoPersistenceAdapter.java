package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.RecorridoRepositorio;
import com.alertamujer.alerts.domain.model.SavedRoute;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los recorridos guardados en PostgreSQL. */
@Component
class RecorridoPersistenceAdapter implements RecorridoRepositorio {

    private final SavedRouteJpaRepository jpa;

    RecorridoPersistenceAdapter(SavedRouteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<SavedRoute> ofUser(Long perfil) {
        return jpa.findByUserProfileIdOrderByCreatedAtDesc(perfil).stream().map(RecorridoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<SavedRoute> find(Long id) {
        return jpa.findById(id).map(RecorridoPersistenceAdapter::aDominio);
    }

    @Override
    public long contar(Long perfil) {
        return jpa.countByUserProfileId(perfil);
    }

    @Override
    public long contarEstrella(Long perfil) {
        return jpa.countByUserProfileIdAndIsStarredTrue(perfil);
    }

    @Override
    public SavedRoute save(SavedRoute recorrido) {
        return aDominio(jpa.save(aFila(recorrido)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static SavedRoute aDominio(SavedRouteJpaEntity f) {
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

    static SavedRouteJpaEntity aFila(SavedRoute d) {
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
