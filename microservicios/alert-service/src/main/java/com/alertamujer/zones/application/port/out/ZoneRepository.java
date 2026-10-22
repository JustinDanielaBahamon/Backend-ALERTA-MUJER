package com.alertamujer.zones.application.port.out;

import com.alertamujer.zones.domain.model.Zone;

import java.util.List;
import java.util.Optional;

/** Output port: the map zones. */
public interface ZoneRepository {

    List<Zone> all();

    Optional<Zone> find(Long id);

    List<Zone> ofCity(String city);

    List<Zone> ofType(String type);

    Zone save(Zone zone);

    void delete(Long id);
}
