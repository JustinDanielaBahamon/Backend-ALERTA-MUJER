package com.alertamujer.zones.adapter.out.persistence;

import com.alertamujer.zones.application.port.out.ZonaRepositorio;
import com.alertamujer.zones.domain.model.Zone;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: las zonas en PostgreSQL. */
@Component
class ZonaPersistenceAdapter implements ZonaRepositorio {

    private final ZoneJpaRepository jpa;

    ZonaPersistenceAdapter(ZoneJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Zone> todas() {
        return jpa.findAll().stream().map(ZonaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<Zone> buscar(Long id) {
        return jpa.findById(id).map(ZonaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Zone> deCiudad(String ciudad) {
        return jpa.findByCity(ciudad).stream().map(ZonaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Zone> deTipo(String tipo) {
        return jpa.findByZoneType(tipo).stream().map(ZonaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Zone guardar(Zone zona) {
        return aDominio(jpa.save(aFila(zona)));
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }

    static Zone aDominio(ZoneJpaEntity f) {
        Zone d = new Zone();
        d.setId(f.getId());
        d.setCreatedByAdminId(f.getCreatedByAdminId());
        d.setName(f.getName());
        d.setZoneType(f.getZoneType());
        d.setRiskLevel(f.getRiskLevel());
        d.setDescription(f.getDescription());
        d.setAddress(f.getAddress());
        d.setCity(f.getCity());
        d.setLatitude(f.getLatitude());
        d.setLongitude(f.getLongitude());
        d.setRadiusMeters(f.getRadiusMeters());
        d.setIsActive(f.getIsActive());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static ZoneJpaEntity aFila(Zone d) {
        ZoneJpaEntity f = new ZoneJpaEntity();
        f.setId(d.getId());
        f.setCreatedByAdminId(d.getCreatedByAdminId());
        f.setName(d.getName());
        f.setZoneType(d.getZoneType());
        f.setRiskLevel(d.getRiskLevel());
        f.setDescription(d.getDescription());
        f.setAddress(d.getAddress());
        f.setCity(d.getCity());
        f.setLatitude(d.getLatitude());
        f.setLongitude(d.getLongitude());
        f.setRadiusMeters(d.getRadiusMeters());
        f.setIsActive(d.getIsActive());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}
