package com.alertamujer.alerts.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos de una alerta que otros módulos pueden conocer. */
public record AlertSummary(Long id, Long userProfileId, String alertType, String activationMethod, String status,
                           String message, LocalDateTime startedAt, BigDecimal latitude, BigDecimal longitude,
                           LocalDateTime endedAt, LocalDateTime cancelledAt) {}
