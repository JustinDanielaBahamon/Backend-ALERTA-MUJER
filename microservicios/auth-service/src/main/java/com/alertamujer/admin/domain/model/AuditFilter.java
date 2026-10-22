package com.alertamujer.admin.domain.model;

import java.time.LocalDate;

/** Filtro de la auditoría. actorAccountId = 0 filtra los cambios hechos por el sistema (sin responsable). */
public record AuditFilter(Long actorAccountId, String entityType, String action, LocalDate from, LocalDate to) {
}
