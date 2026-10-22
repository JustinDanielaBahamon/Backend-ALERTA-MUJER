package com.alertamujer.alerts.domain.model;

import java.time.LocalDateTime;

/** Cambios que la dueña envía sobre su alerta. Un campo null se deja como estaba. */
public record AlertChanges(String alertType, String activationMethod, String status, String message,
                            LocalDateTime endedAt, LocalDateTime cancelledAt) {
}
