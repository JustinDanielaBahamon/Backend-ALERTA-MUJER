package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;

/** Data of a frequent place. On update, a null field is left as it was. */
public record PlaceData(String name, String address, String city, BigDecimal latitude, BigDecimal longitude,
                         String notes, String riskLevel, Boolean isActive) {
}
