package com.alertamujer.resources.adapter.in.web.dto;

import com.alertamujer.resources.domain.model.ResourceData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Body to create or edit a help resource (admin panel). */
public record EmergencyResourceRequest(String name, String resourceType, String telephone, String secondaryTelephone,
                                       String email, String address, String city, BigDecimal latitude,
                                       BigDecimal longitude, String description, @JsonProperty("isActive") Boolean isActive) {

    public ResourceData toData() {
        return new ResourceData(name, resourceType, telephone, secondaryTelephone, email, address, city, latitude, longitude,
                description, isActive);
    }
}
