package com.alertamujer.zones.domain.model;

import java.math.BigDecimal;

/** Datos de una zona que envía el panel. createdByAdminId solo se usa al crearla. */
public record DatosZona(Integer createdByAdminId, String name, String zoneType, String riskLevel, String description,
                        String address, String city, BigDecimal latitude, BigDecimal longitude, BigDecimal radiusMeters,
                        Boolean isActive) {
}
