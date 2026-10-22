package com.alertamujer.alerts.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Point of an alert route. */
public record LocationPoint(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}
