package com.alertamujer.resources.domain.model;

import java.math.BigDecimal;

/** Datos de un recurso de ayuda que envía el panel. */
public record ResourceData(String name, String resourceType, String telephone, String secondaryTelephone, String email,
                           String address, String city, BigDecimal latitude, BigDecimal longitude, String description,
                           Boolean isActive) {
}
