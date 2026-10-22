package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AlertReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los informes de alertas. */
public interface ReportExportRepository {

    List<AlertReport> all();

    Optional<AlertReport> find(Long id);

    AlertReport save(AlertReport report);

    void delete(Long id);
}
