package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.FrequentPlaceRepository;
import com.alertamujer.alerts.domain.model.FrequentLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: frequent places in PostgreSQL. */
@Component
class FrequentPlacePersistenceAdapter implements FrequentPlaceRepository {

    private final FrequentLocationJpaRepository jpa;

    FrequentPlacePersistenceAdapter(FrequentLocationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<FrequentLocation> all() {
        return jpa.findAll().stream().map(FrequentPlacePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<FrequentLocation> activeOfUser(Long profile) {
        return jpa.findByUserProfileIdAndIsActiveTrue(profile).stream().map(FrequentPlacePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<FrequentLocation> find(Long id) {
        return jpa.findById(id).map(FrequentPlacePersistenceAdapter::toDomain);
    }

    @Override
    public FrequentLocation save(FrequentLocation place) {
        return toDomain(jpa.save(toRow(place)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static FrequentLocation toDomain(FrequentLocationJpaEntity f) {
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

    static FrequentLocationJpaEntity toRow(FrequentLocation d) {
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
