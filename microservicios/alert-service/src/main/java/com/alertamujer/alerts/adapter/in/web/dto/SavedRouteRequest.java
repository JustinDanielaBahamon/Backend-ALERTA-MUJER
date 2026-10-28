package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.application.port.in.NewRoute;
import com.alertamujer.alerts.domain.model.RoutePoint;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Body to create a route. The owner and the star are decided by the backend, not the client. */
public record SavedRouteRequest(String name, String startLabel, String endLabel, String city, String department,
                                String country, BigDecimal distanceKm, Integer estimatedMinutes,
                                List<RoutePoint> points, @JsonProperty("isManual") Boolean isManual) {

    public NewRoute toNew() {
        return new NewRoute(name, startLabel, endLabel, city, department, country, distanceKm, estimatedMinutes,
                points, isManual);
    }
}
