package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.RoutePoint;

import java.math.BigDecimal;
import java.util.List;

/** Datos para guardar un recorrido. La dueña y la estrella las decide el backend. */
public record NuevoRecorrido(String name, String startLabel, String endLabel, String city, String department,
                             String country, BigDecimal distanceKm, Integer estimatedMinutes,
                             List<RoutePoint> points, Boolean isManual) {
}
