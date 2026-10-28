package com.alertamujer.zones.domain.model;

import java.math.BigDecimal;

/** Zone data sent by the panel. createdByAdminId is only used when creating it. */
public record ZoneData(Integer createdByAdminId, String name, String zoneType, String riskLevel, String description,
                        String address, String city, BigDecimal latitude, BigDecimal longitude, BigDecimal radiusMeters,
                        Boolean isActive) {
}
