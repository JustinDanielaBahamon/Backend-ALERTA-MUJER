package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AlertReport;

import java.util.List;
import java.util.Optional;

/** Output port: the alert reports. */
public interface ReportExportRepository {

    List<AlertReport> all();

    Optional<AlertReport> find(Long id);

    AlertReport save(AlertReport report);

    void delete(Long id);
}
