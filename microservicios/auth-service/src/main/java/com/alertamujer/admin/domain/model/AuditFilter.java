package com.alertamujer.admin.domain.model;

import java.time.LocalDate;

/** Audit filter. actorAccountId = 0 filters the changes made by the system (no actor). */
public record AuditFilter(Long actorAccountId, String entityType, String action, LocalDate from, LocalDate to) {
}
