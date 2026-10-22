package com.alertamujer.admin.domain.model;

/** Changes to a moderation case; a null field is left untouched. */
public record ReportChanges(String reason, String description, String severity, String status, String resolutionNotes) {
}
