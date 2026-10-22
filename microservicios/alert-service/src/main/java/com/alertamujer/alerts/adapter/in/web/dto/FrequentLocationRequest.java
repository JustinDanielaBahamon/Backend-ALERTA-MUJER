package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.PlaceData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Body para crear o modificar (parcialmente) un lugar frecuente. */
public record FrequentLocationRequest(String name, String address, String city, BigDecimal latitude,
                                      BigDecimal longitude, String notes, String riskLevel,
                                      @JsonProperty("isActive") Boolean isActive) {

    public PlaceData toData() {
        return new PlaceData(name, address, city, latitude, longitude, notes, riskLevel, isActive);
    }
}
