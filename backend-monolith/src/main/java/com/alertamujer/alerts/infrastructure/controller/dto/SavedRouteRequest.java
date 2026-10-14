package com.alertamujer.alerts.infrastructure.controller.dto;

import com.alertamujer.alerts.domain.model.RoutePoint;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * Body para crear un recorrido. El dueño y la estrella los decide el backend, no el cliente.
 */
@Data
public class SavedRouteRequest {
    private String name;
    private String startLabel;
    private String endLabel;
    private String city;
    private String department;
    private String country;
    private BigDecimal distanceKm;
    private Integer estimatedMinutes;
    private List<RoutePoint> points;
    private Boolean isManual;
}
