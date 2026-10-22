package com.alertamujer.zones.application.port.out;

import com.alertamujer.zones.domain.model.Zone;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las zonas del mapa. */
public interface ZonaRepositorio {

    List<Zone> todas();

    Optional<Zone> find(Long id);

    List<Zone> deCiudad(String ciudad);

    List<Zone> deTipo(String type);

    Zone save(Zone zona);

    void delete(Long id);
}
