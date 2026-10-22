package com.alertamujer.zones.application.port.out;

import com.alertamujer.zones.domain.model.ZoneReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los reportes de las usuarias sobre las zonas. */
public interface ReporteZonaRepositorio {

    List<ZoneReport> todos();

    Optional<ZoneReport> find(Long id);

    List<ZoneReport> deZona(Long zoneId);

    List<ZoneReport> dePerfil(Long perfil);

    List<ZoneReport> conEstado(String estado);

    ZoneReport save(ZoneReport reporte);

    void delete(Long id);
}
