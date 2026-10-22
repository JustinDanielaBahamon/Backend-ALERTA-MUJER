package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.admin.domain.model.ReportChanges;

/** Body para crear (reportedUserEmail, reason, description, severity) o actualizar un caso de moderación. */
public record UserReportRequest(String reportedUserEmail, String reason, String description, String severity,
                                String status, String resolutionNotes) {

    public ReportChanges toChanges() {
        return new ReportChanges(reason, description, severity, status, resolutionNotes);
    }
}
