package com.alertamujer.zones.application.port.in;

import com.alertamujer.zones.domain.model.DatosZona;
import com.alertamujer.zones.domain.model.Zone;

import java.util.List;
import java.util.Optional;

/** Zonas del mapa: las ve cualquier usuaria con sesión y las edita la administración. */
public interface ZonasUseCase {

    List<Zone> todas();

    Optional<Zone> una(Long id);

    List<Zone> deCiudad(String ciudad);

    List<Zone> deTipo(String type);

    Zone create(DatosZona data);

    Zone reemplazar(Long id, DatosZona data);

    void delete(Long id);

    /** Zonas activas (estadísticas del panel). */
    long contarActivas();
}
