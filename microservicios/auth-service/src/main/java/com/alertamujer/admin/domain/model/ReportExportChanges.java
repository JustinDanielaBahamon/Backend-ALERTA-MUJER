package com.alertamujer.admin.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/** Datos de un informe de alertas; un campo null no se toca. */
public record ReportExportChanges(String name, String city, String status, String userLabel, String event, String description,
                             String format, List<Long> alertIds, LocalDateTime fromDate, LocalDateTime toDate,
                             String alertStatus) {
}
