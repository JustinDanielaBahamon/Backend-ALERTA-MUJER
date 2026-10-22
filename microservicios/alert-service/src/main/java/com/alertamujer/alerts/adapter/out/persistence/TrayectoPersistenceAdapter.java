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
    public SafeTrip guardar(SafeTrip trayecto) {
        return aDominio(jpa.save(aFila(trayecto)));
    }

    @Override
    public Optional<SafeTrip> buscar(Long id) {
        return jpa.findById(id).map(TrayectoPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<SafeTrip> activoDe(Long perfil) {
        return jpa.findFirstByUserProfileIdAndStatus(perfil, SafeTrip.ACTIVO).map(TrayectoPersistenceAdapter::aDominio);
    }

    @Override
    public List<SafeTrip> activosQueLleganAntesDe(LocalDateTime momento) {
        return jpa.findByStatusAndExpectedArrivalBefore(SafeTrip.ACTIVO, momento).stream()
                .map(TrayectoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public long iniciadosDesde(Long perfil, LocalDateTime desde) {
        return jpa.countByUserProfileIdAndCreatedAtAfter(perfil, desde);
    }

    static SafeTrip aDominio(SafeTripJpaEntity f) {
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

    static SafeTripJpaEntity aFila(SafeTrip d) {
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
