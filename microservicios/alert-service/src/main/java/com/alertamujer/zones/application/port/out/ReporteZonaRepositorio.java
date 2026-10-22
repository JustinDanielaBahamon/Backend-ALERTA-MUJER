package com.alertamujer.zones.application.port.out;

import com.alertamujer.zones.domain.model.ZoneReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los reportes de las usuarias sobre las zonas. */
public interface ReporteZonaRepositorio {

    List<ZoneReport> all();

    Optional<ZoneReport> find(Long id);

    List<ZoneReport> deZona(Long zoneId);

    List<ZoneReport> dePerfil(Long profile);

    List<ZoneReport> withStatus(String status);

    ZoneReport save(ZoneReport userReport);

    void delete(Long id);
}
