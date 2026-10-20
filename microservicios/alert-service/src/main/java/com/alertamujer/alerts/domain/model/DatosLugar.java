package com.alertamujer.alerts.domain.model;

import java.math.BigDecimal;

/** Datos de un lugar frecuente. Al actualizar, un campo null se deja como estaba. */
public record DatosLugar(String name, String address, String city, BigDecimal latitude, BigDecimal longitude,
                         String notes, String riskLevel, Boolean isActive) {
}
