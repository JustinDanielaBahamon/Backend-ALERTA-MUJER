package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.TrayectoRepositorio;
import com.alertamujer.alerts.domain.model.SafeTrip;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los trayectos de "Avísame si no llego" en PostgreSQL. */
@Component
class TrayectoPersistenceAdapter implements TrayectoRepositorio {

    private final SafeTripJpaRepository jpa;

    TrayectoPersistenceAdapter(SafeTripJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SafeTrip save(SafeTrip trayecto) {
        return toDomain(jpa.save(toRow(trayecto)));
    }

    @Override
    public Optional<SafeTrip> find(Long id) {
        return jpa.findById(id).map(TrayectoPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<SafeTrip> activoDe(Long profile) {
        return jpa.findFirstByUserProfileIdAndStatus(profile, SafeTrip.ACTIVO).map(TrayectoPersistenceAdapter::toDomain);
    }

    @Override
    public List<SafeTrip> activosQueLleganAntesDe(LocalDateTime momento) {
        return jpa.findByStatusAndExpectedArrivalBefore(SafeTrip.ACTIVO, momento).stream()
                .map(TrayectoPersistenceAdapter::toDomain).toList();
    }

    @Override
    public long iniciadosDesde(Long profile, LocalDateTime desde) {
        return jpa.countByUserProfileIdAndCreatedAtAfter(profile, desde);
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
        return f;
    }
}
