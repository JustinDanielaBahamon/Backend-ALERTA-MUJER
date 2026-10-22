package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AlertReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los informes de alertas. */
public interface InformeRepositorio {

    List<AlertReport> todos();

    Optional<AlertReport> find(Long id);

    AlertReport save(AlertReport informe);

    void delete(Long id);
}
