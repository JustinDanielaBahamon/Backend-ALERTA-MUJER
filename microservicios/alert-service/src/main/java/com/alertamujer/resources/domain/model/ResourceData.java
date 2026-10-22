package com.alertamujer.resources.domain.model;

import java.math.BigDecimal;

/** Data of a help resource sent by the panel. */
public record ResourceData(String name, String resourceType, String telephone, String secondaryTelephone, String email,
                           String address, String city, BigDecimal latitude, BigDecimal longitude, String description,
                           Boolean isActive) {
}
