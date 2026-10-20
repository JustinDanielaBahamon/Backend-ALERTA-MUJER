package com.alertamujer.alerts.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Cambios parciales sobre un recorrido: renombrar y/o marcar o desmarcar estrella. */
public record SavedRouteUpdateRequest(String name, @JsonProperty("isStarred") Boolean isStarred) {
}
