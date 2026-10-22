package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.alerts.domain.model.LocationLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output adapter: location history in PostgreSQL. */
@Component
class LocationPersistenceAdapter implements LocationRepository {

    private final LocationLogJpaRepository jpa;

    LocationPersistenceAdapter(LocationLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LocationLog save(LocationLog location) {
        return toDomain(jpa.save(toRow(location)));
    }

    @Override
    public List<LocationLog> recent(Long profile, int howMany) {
        return jpa.findByUserProfileIdOrderByRecordedAtDesc(profile, PageRequest.of(0, howMany)).stream()
                .map(LocationPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<LocationLog> latest(Long profile) {
        return jpa.findFirstByUserProfileIdOrderByRecordedAtDesc(profile).map(LocationPersistenceAdapter::toDomain);
    }

    @Override
    public List<LocationLog> ofAlert(Long alertId) {
        return jpa.findByAlertIdOrderByRecordedAtAsc(alertId).stream().map(LocationPersistenceAdapter::toDomain).toList();
    }

    @Override
    public int deleteWithoutAlertBefore(LocalDateTime date) {
        return jpa.deleteWithoutAlertBefore(date);
    }

    @Override
    public int deleteFromAlertsBefore(LocalDateTime date) {
        return jpa.deleteFromAlertsBefore(date);
    }

    static LocationLog toDomain(LocationLogJpaEntity f) {
        LocationLog d = new LocationLog();
        d.setId(f.getId());
        d.setUserProfileId(f.getUserProfileId());
        d.setAlertId(f.getAlertId());
        d.setLatitude(f.getLatitude());
        d.setLongitude(f.getLongitude());
        d.setAccuracy(f.getAccuracy());
        d.setRecordedAt(f.getRecordedAt());
        return d;
    }

    static LocationLogJpaEntity toRow(LocationLog d) {
        LocationLogJpaEntity f = new LocationLogJpaEntity();
        f.setId(d.getId());
        f.setUserProfileId(d.getUserProfileId());
        f.setAlertId(d.getAlertId());
        f.setLatitude(d.getLatitude());
        f.setLongitude(d.getLongitude());
        f.setAccuracy(d.getAccuracy());
        f.setRecordedAt(d.getRecordedAt());
        return f;
    }
}
