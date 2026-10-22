package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.ReportExportChanges;

import java.time.LocalDateTime;
import java.util.List;

/** Panel alert reports: saved with a name and their alerts and exported to PDF or Excel. */
public interface ReportExportUseCase {

    List<AlertReportView> listAll();

    AlertReportView get(Long id);

    ReportStats stats();

    AlertReportView create(ReportExportChanges changes, Long adminUserId);

    AlertReportView update(Long id, ReportExportChanges changes);

    void delete(Long id);

    /** Report file with its alerts (pdf or csv); adds one export to its counter. */
    byte[] export(Long id, boolean pdf);

    record AlertReportView(Long id, String name, String city, String status, String userLabel, String event,
                           String description, String format, List<Long> alertIds, LocalDateTime fromDate,
                           LocalDateTime toDate, String alertStatus, int alertCount, Integer exportCount,
                           String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    record ReportStats(long totalReports, long totalAlerts, long activeZones, long exports) {}
}
