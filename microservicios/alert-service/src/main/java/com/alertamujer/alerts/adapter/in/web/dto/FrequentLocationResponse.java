package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.FrequentLocation;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Same order as always: @JsonProperty would move the "is..." ones to the start
@JsonPropertyOrder({"id", "userProfileId", "name", "address", "city", "latitude", "longitude", "notes", "riskLevel",
        "isActive", "createdAt", "updatedAt"})
public record FrequentLocationResponse(Long id, Long userProfileId, String name, String address, String city,
                                       BigDecimal latitude, BigDecimal longitude, String notes, String riskLevel,
                                       @JsonProperty("isActive") Boolean isActive,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static FrequentLocationResponse of(FrequentLocation l) {
        return new FrequentLocationResponse(l.getId(), l.getUserProfileId(), l.getName(), l.getAddress(), l.getCity(),
                l.getLatitude(), l.getLongitude(), l.getNotes(), l.getRiskLevel(), l.getIsActive(),
                l.getCreatedAt(), l.getUpdatedAt());
    }
}
