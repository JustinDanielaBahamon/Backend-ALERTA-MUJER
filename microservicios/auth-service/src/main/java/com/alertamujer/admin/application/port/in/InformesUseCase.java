package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.CambiosInforme;

import java.time.LocalDateTime;
import java.util.List;

/** Informes de alertas del panel: se guardan con un nombre y sus alertas y se exportan a PDF o Excel. */
public interface InformesUseCase {

    List<AlertReportView> listAll();

    AlertReportView obtener(Long id);

    ReportStats stats();

    AlertReportView create(CambiosInforme cambios, Long adminUserId);

    AlertReportView actualizar(Long id, CambiosInforme cambios);

    void delete(Long id);

    /** Archivo del informe con sus alertas (pdf o csv); suma una exportación a su contador. */
    byte[] exportar(Long id, boolean pdf);

    record AlertReportView(Long id, String name, String city, String status, String userLabel, String event,
                           String description, String format, List<Long> alertIds, LocalDateTime fromDate,
                           LocalDateTime toDate, String alertStatus, int alertCount, Integer exportCount,
                           String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    record ReportStats(long totalReports, long totalAlerts, long activeZones, long exports) {}
}
