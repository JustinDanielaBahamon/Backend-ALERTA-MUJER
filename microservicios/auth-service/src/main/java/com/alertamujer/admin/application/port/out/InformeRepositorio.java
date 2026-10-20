package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AlertReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los informes de alertas. */
public interface InformeRepositorio {

    List<AlertReport> todos();

    Optional<AlertReport> buscar(Long id);

    AlertReport guardar(AlertReport informe);

    void eliminar(Long id);
}
