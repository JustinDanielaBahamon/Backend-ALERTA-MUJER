package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.RoutePoint;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Same order as always: @JsonProperty would move the "is..." ones to the start
@JsonPropertyOrder({"id", "userProfileId", "name", "startLabel", "endLabel", "city", "department", "country",
        "distanceKm", "estimatedMinutes", "points", "isStarred", "isManual", "createdAt", "updatedAt"})
public record SavedRouteResponse(Long id, Long userProfileId, String name, String startLabel, String endLabel,
                                 String city, String department, String country, BigDecimal distanceKm,
                                 Integer estimatedMinutes, List<RoutePoint> points,
                                 @JsonProperty("isStarred") Boolean isStarred,
                                 @JsonProperty("isManual") Boolean isManual,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static SavedRouteResponse of(SavedRoute r) {
        return new SavedRouteResponse(r.getId(), r.getUserProfileId(), r.getName(), r.getStartLabel(), r.getEndLabel(),
                r.getCity(), r.getDepartment(), r.getCountry(), r.getDistanceKm(), r.getEstimatedMinutes(), r.getPoints(),
                r.getIsStarred(), r.getIsManual(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
