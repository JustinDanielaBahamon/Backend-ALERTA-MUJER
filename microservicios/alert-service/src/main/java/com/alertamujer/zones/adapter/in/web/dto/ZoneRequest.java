package com.alertamujer.zones.adapter.in.web.dto;

import com.alertamujer.zones.domain.model.ZoneData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Body para crear o editar una zona (panel de administración). */
public record ZoneRequest(Integer createdByAdminId, String name, String zoneType, String riskLevel, String description,
                          String address, String city, BigDecimal latitude, BigDecimal longitude, BigDecimal radiusMeters,
                          @JsonProperty("isActive") Boolean isActive) {

    public ZoneData toData() {
        return new ZoneData(createdByAdminId, name, zoneType, riskLevel, description, address, city, latitude, longitude,
                radiusMeters, isActive);
    }
}
