package com.alertamujer.zones.adapter.in.web.dto;

import com.alertamujer.zones.domain.model.Zone;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Same order as always: @JsonProperty would move "isActive" to the start
@JsonPropertyOrder({"id", "createdByAdminId", "name", "zoneType", "riskLevel", "description", "address", "city", "latitude",
        "longitude", "radiusMeters", "isActive", "createdAt", "updatedAt"})
public record ZoneResponse(Long id, Integer createdByAdminId, String name, String zoneType, String riskLevel,
                           String description, String address, String city, BigDecimal latitude, BigDecimal longitude,
                           BigDecimal radiusMeters, @JsonProperty("isActive") Boolean isActive, LocalDateTime createdAt,
                           LocalDateTime updatedAt) {

    public static ZoneResponse of(Zone z) {
        return new ZoneResponse(z.getId(), z.getCreatedByAdminId(), z.getName(), z.getZoneType(), z.getRiskLevel(),
                z.getDescription(), z.getAddress(), z.getCity(), z.getLatitude(), z.getLongitude(), z.getRadiusMeters(),
                z.getIsActive(), z.getCreatedAt(), z.getUpdatedAt());
    }
}
