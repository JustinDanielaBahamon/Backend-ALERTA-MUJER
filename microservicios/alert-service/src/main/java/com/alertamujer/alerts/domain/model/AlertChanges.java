package com.alertamujer.alerts.domain.model;

import java.time.LocalDateTime;

/** Changes the owner sends about her alert. A null field is left as it was. */
public record AlertChanges(String alertType, String activationMethod, String status, String message,
                            LocalDateTime endedAt, LocalDateTime cancelledAt) {
}
