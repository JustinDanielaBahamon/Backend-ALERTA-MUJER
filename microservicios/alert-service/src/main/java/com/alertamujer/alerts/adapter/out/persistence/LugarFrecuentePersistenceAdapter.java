package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.LugarFrecuenteRepositorio;
import com.alertamujer.alerts.domain.model.FrequentLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los lugares frecuentes en PostgreSQL. */
@Component
class LugarFrecuentePersistenceAdapter implements LugarFrecuenteRepositorio {

    private final FrequentLocationJpaRepository jpa;

    LugarFrecuentePersistenceAdapter(FrequentLocationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<FrequentLocation> todos() {
        return jpa.findAll().stream().map(LugarFrecuentePersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<FrequentLocation> activosDeLaUsuaria(Long perfil) {
        return jpa.findByUserProfileIdAndIsActiveTrue(perfil).stream().map(LugarFrecuentePersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<FrequentLocation> buscar(Long id) {
        return jpa.findById(id).map(LugarFrecuentePersistenceAdapter::aDominio);
    }

    @Override
    public FrequentLocation guardar(FrequentLocation lugar) {
        return aDominio(jpa.save(aFila(lugar)));
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    static FrequentLocation aDominio(FrequentLocationJpaEntity f) {
        FrequentLocation d = new FrequentLocation();
        d.setId(f.getId());
        d.setUserProfileId(f.getUserProfileId());
        d.setName(f.getName());
        d.setAddress(f.getAddress());
        d.setCity(f.getCity());
        d.setLatitude(f.getLatitude());
        d.setLongitude(f.getLongitude());
        d.setNotes(f.getNotes());
        d.setRiskLevel(f.getRiskLevel());
        d.setIsActive(f.getIsActive());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static FrequentLocationJpaEntity aFila(FrequentLocation d) {
        FrequentLocationJpaEntity f = new FrequentLocationJpaEntity();
        f.setId(d.getId());
        f.setUserProfileId(d.getUserProfileId());
        f.setName(d.getName());
        f.setAddress(d.getAddress());
        f.setCity(d.getCity());
        f.setLatitude(d.getLatitude());
        f.setLongitude(d.getLongitude());
        f.setNotes(d.getNotes());
        f.setRiskLevel(d.getRiskLevel());
        f.setIsActive(d.getIsActive());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
