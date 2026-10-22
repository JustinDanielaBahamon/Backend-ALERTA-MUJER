package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.ReportExportChanges;

import java.time.LocalDateTime;
import java.util.List;

/** Informes de alertas del panel: se guardan con un nombre y sus alertas y se exportan a PDF o Excel. */
public interface ReportExportUseCase {

    List<AlertReportView> listAll();

    AlertReportView get(Long id);

    ReportStats stats();

    AlertReportView create(ReportExportChanges changes, Long adminUserId);

    AlertReportView update(Long id, ReportExportChanges changes);

    void delete(Long id);

    /** Archivo del informe con sus alertas (pdf o csv); suma una exportación a su contador. */
    byte[] export(Long id, boolean pdf);

    record AlertReportView(Long id, String name, String city, String status, String userLabel, String event,
                           String description, String format, List<Long> alertIds, LocalDateTime fromDate,
                           LocalDateTime toDate, String alertStatus, int alertCount, Integer exportCount,
                           String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    record ReportStats(long totalReports, long totalAlerts, long activeZones, long exports) {}
}
