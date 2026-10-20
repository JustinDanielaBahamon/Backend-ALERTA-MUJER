package com.alertamujer.resources.adapter.in.web.dto;

import com.alertamujer.resources.domain.model.EmergencyResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Mismo orden de siempre: @JsonProperty movería "isActive" al inicio
@JsonPropertyOrder({"id", "name", "resourceType", "telephone", "secondaryTelephone", "email", "address", "city", "latitude",
        "longitude", "description", "isActive", "createdAt", "updatedAt"})
public record EmergencyResourceResponse(Long id, String name, String resourceType, String telephone,
                                        String secondaryTelephone, String email, String address, String city,
                                        BigDecimal latitude, BigDecimal longitude, String description,
                                        @JsonProperty("isActive") Boolean isActive, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {

    public static EmergencyResourceResponse of(EmergencyResource r) {
        return new EmergencyResourceResponse(r.getId(), r.getName(), r.getResourceType(), r.getTelephone(),
                r.getSecondaryTelephone(), r.getEmail(), r.getAddress(), r.getCity(), r.getLatitude(), r.getLongitude(),
                r.getDescription(), r.getIsActive(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
