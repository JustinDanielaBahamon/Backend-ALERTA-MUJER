package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.application.port.in.ReportExportUseCase.AlertReportView;
import com.alertamujer.alerts.api.AdminAlertView;

import java.util.List;

/** Output port: generates the file of a report with its alerts. */
public interface ReportExporterPort {

    byte[] pdf(AlertReportView report, List<AdminAlertView> alerts);

    /** CSV with ";" and UTF-8 BOM, which is how Excel in Spanish opens it correctly. */
    byte[] csv(AlertReportView report, List<AdminAlertView> alerts);
}
