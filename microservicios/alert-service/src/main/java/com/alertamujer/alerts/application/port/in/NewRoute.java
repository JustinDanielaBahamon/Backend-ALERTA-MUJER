package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.RoutePoint;

import java.math.BigDecimal;
import java.util.List;

/** Data to save a route. The owner and the star are decided by the backend. */
public record NewRoute(String name, String startLabel, String endLabel, String city, String department,
                             String country, BigDecimal distanceKm, Integer estimatedMinutes,
                             List<RoutePoint> points, Boolean isManual) {
}
