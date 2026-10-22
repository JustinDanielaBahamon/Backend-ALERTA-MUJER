package com.alertamujer.resources.adapter.in.web.dto;

import com.alertamujer.resources.domain.model.DatosRecurso;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Body para crear o editar un recurso de ayuda (panel de administración). */
public record EmergencyResourceRequest(String name, String resourceType, String telephone, String secondaryTelephone,
                                       String email, String address, String city, BigDecimal latitude,
                                       BigDecimal longitude, String description, @JsonProperty("isActive") Boolean isActive) {

    public DatosRecurso toData() {
        return new DatosRecurso(name, resourceType, telephone, secondaryTelephone, email, address, city, latitude, longitude,
                description, isActive);
    }
}
