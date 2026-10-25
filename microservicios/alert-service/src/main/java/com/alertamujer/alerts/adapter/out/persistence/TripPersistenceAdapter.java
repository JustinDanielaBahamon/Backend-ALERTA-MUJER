package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.TripRepository;
import com.alertamujer.alerts.domain.model.SafeTrip;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output adapter: "Avísame si no llego" trips in PostgreSQL. */
@Component
class TripPersistenceAdapter implements TripRepository {

    private final SafeTripJpaRepository jpa;

    TripPersistenceAdapter(SafeTripJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SafeTrip save(SafeTrip trip) {
        return toDomain(jpa.save(toRow(trip)));
    }

    @Override
    public Optional<SafeTrip> find(Long id) {
        return jpa.findById(id).map(TripPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<SafeTrip> activeOf(Long profile) {
        return jpa.findFirstByUserProfileIdAndStatus(profile, SafeTrip.ACTIVE).map(TripPersistenceAdapter::toDomain);
    }

    @Override
    public List<SafeTrip> allActive() {
        return jpa.findByStatus(SafeTrip.ACTIVE).stream().map(TripPersistenceAdapter::toDomain).toList();
    }

    @Override
    public long startedSince(Long profile, LocalDateTime since) {
        return jpa.countByUserProfileIdAndCreatedAtAfter(profile, since);
    }

    static SafeTrip toDomain(SafeTripJpaEntity f) {
        SafeTrip d = new SafeTrip();
        d.setId(f.getId());
        d.setUserProfileId(f.getUserProfileId());
        d.setUserId(f.getUserId());
        d.setDeviceId(f.getDeviceId());
        d.setDestination(f.getDestination());
        d.setDestinationLatitude(f.getDestinationLatitude());
        d.setDestinationLongitude(f.getDestinationLongitude());
        d.setStartedAt(f.getStartedAt());
        d.setExpectedArrival(f.getExpectedArrival());
        d.setStatus(f.getStatus());
        d.setReminderSent(f.isReminderSent());
        d.setAlertId(f.getAlertId());
        d.setEndedAt(f.getEndedAt());
        d.setCreatedAt(f.getCreatedAt());
        d.setLastSeenAt(f.getLastSeenAt());
        d.setLastLatitude(f.getLastLatitude());
        d.setLastLongitude(f.getLastLongitude());
        d.setAlertReason(f.getAlertReason());
        d.setMode(f.getMode());
        d.setLowBatteryAt(f.getLowBatteryAt());
        return d;
    }

    static SafeTripJpaEntity toRow(SafeTrip d) {
        SafeTripJpaEntity f = new SafeTripJpaEntity();
        f.setId(d.getId());
        f.setUserProfileId(d.getUserProfileId());
        f.setUserId(d.getUserId());
        f.setDeviceId(d.getDeviceId());
        f.setDestination(d.getDestination());
        f.setDestinationLatitude(d.getDestinationLatitude());
        f.setDestinationLongitude(d.getDestinationLongitude());
        f.setStartedAt(d.getStartedAt());
        f.setExpectedArrival(d.getExpectedArrival());
        f.setStatus(d.getStatus());
        f.setReminderSent(d.isReminderSent());
        f.setAlertId(d.getAlertId());
        f.setEndedAt(d.getEndedAt());
        f.setCreatedAt(d.getCreatedAt());
        f.setLastSeenAt(d.getLastSeenAt());
        f.setLastLatitude(d.getLastLatitude());
        f.setLastLongitude(d.getLastLongitude());
        f.setAlertReason(d.getAlertReason());
        f.setMode(d.getMode());
        f.setLowBatteryAt(d.getLowBatteryAt());
        return f;
    }
}
