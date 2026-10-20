package com.alertamujer.alerts.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Punto del recorrido de una alerta. */
public record LocationPoint(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}
