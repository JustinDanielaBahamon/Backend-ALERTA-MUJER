package com.alertamujer.alerts.domain.model;

/**
 * Punto GPS de un recorrido guardado. Se persiste como elemento del arreglo JSONB saved_route.points.
 */
public record RoutePoint(Double latitude, Double longitude, String timestamp) {
}
