package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import com.alertamujer.alerts.domain.model.LocationLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: el historial de ubicaciones en PostgreSQL. */
@Component
class UbicacionPersistenceAdapter implements UbicacionRepositorio {

    private final LocationLogJpaRepository jpa;

    UbicacionPersistenceAdapter(LocationLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LocationLog guardar(LocationLog ubicacion) {
        return aDominio(jpa.save(aFila(ubicacion)));
    }

    @Override
    public List<LocationLog> recientes(Long perfil, int cuantas) {
        return jpa.findByUserProfileIdOrderByRecordedAtDesc(perfil, PageRequest.of(0, cuantas)).stream()
                .map(UbicacionPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<LocationLog> ultima(Long perfil) {
        return jpa.findFirstByUserProfileIdOrderByRecordedAtDesc(perfil).map(UbicacionPersistenceAdapter::aDominio);
    }

    @Override
    public List<LocationLog> deLaAlerta(Long alertId) {
        return jpa.findByAlertIdOrderByRecordedAtAsc(alertId).stream().map(UbicacionPersistenceAdapter::aDominio).toList();
    }

    @Override
    public int borrarSinAlertaAntesDe(LocalDateTime fecha) {
        return jpa.borrarSinAlertaAntesDe(fecha);
    }

    @Override
    public int borrarDeAlertasAntesDe(LocalDateTime fecha) {
        return jpa.borrarDeAlertasAntesDe(fecha);
    }

    static LocationLog aDominio(LocationLogJpaEntity f) {
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

    static LocationLogJpaEntity aFila(LocationLog d) {
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
