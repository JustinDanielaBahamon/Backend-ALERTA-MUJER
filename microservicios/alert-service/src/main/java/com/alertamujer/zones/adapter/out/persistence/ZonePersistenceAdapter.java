package com.alertamujer.zones.adapter.out.persistence;

import com.alertamujer.zones.application.port.out.ZoneRepository;
import com.alertamujer.zones.domain.model.Zone;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: zones in PostgreSQL. */
@Component
class ZonePersistenceAdapter implements ZoneRepository {

    private final ZoneJpaRepository jpa;

    ZonePersistenceAdapter(ZoneJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Zone> all() {
        return jpa.findAll().stream().map(ZonePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<Zone> find(Long id) {
        return jpa.findById(id).map(ZonePersistenceAdapter::toDomain);
    }

    @Override
    public List<Zone> ofCity(String city) {
        return jpa.findByCity(city).stream().map(ZonePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Zone> ofType(String type) {
        return jpa.findByZoneType(type).stream().map(ZonePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Zone save(Zone zone) {
        return toDomain(jpa.save(toRow(zone)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static Zone toDomain(ZoneJpaEntity f) {
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

    static ZoneJpaEntity toRow(Zone d) {
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
