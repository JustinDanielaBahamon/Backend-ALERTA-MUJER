package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.alerts.api.AdminAlertView;

import java.util.List;

/** Puerto de salida: genera el archivo de un informe con sus alertas. */
public interface ReportExporterPort {

    byte[] pdf(AlertReportView report, List<AdminAlertView> alerts);

    /** CSV con ";" y BOM UTF-8, que es como Excel en español lo abre bien. */
    byte[] csv(AlertReportView report, List<AdminAlertView> alerts);
}
