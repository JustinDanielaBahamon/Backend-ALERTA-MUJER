package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.admin.domain.model.ReportChanges;

/** Body to create (reportedUserEmail, reason, description, severity) or update a moderation case. */
public record UserReportRequest(String reportedUserEmail, String reason, String description, String severity,
                                String status, String resolutionNotes) {

    public ReportChanges toChanges() {
        return new ReportChanges(reason, description, severity, status, resolutionNotes);
    }
}
