package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.admin.domain.model.CambiosReporte;

/** Body para crear (reportedUserEmail, reason, description, severity) o actualizar un caso de moderación. */
public record UserReportRequest(String reportedUserEmail, String reason, String description, String severity,
                                String status, String resolutionNotes) {

    public CambiosReporte aCambios() {
        return new CambiosReporte(reason, description, severity, status, resolutionNotes);
    }
}
