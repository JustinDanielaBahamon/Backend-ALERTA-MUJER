package com.alertamujer.zones.application.port.in;

import com.alertamujer.zones.domain.model.ZoneData;
import com.alertamujer.zones.domain.model.Zone;

import java.util.List;
import java.util.Optional;

/** Zonas del mapa: las ve cualquier usuaria con sesión y las edita la administración. */
public interface ZonesUseCase {

    List<Zone> all();

    Optional<Zone> one(Long id);

    List<Zone> ofCity(String city);

    List<Zone> ofType(String type);

    Zone create(ZoneData data);

    Zone replace(Long id, ZoneData data);

    void delete(Long id);

    /** Zonas activas (estadísticas del panel). */
    long countActive();
}
