package com.alertamujer.alerts.domain.model;

/**
 * GPS point of a saved route. Persisted as an element of the JSONB array saved_route.points.
 */
public record RoutePoint(Double latitude, Double longitude, String timestamp) {
}
