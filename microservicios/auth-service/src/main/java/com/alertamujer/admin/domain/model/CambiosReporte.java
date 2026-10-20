package com.alertamujer.admin.domain.model;

/** Cambios sobre un caso de moderación; un campo null no se toca. */
public record CambiosReporte(String reason, String description, String severity, String status, String resolutionNotes) {
}
