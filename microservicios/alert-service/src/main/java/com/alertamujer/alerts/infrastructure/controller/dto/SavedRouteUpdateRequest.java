package com.alertamujer.alerts.infrastructure.controller.dto;

import lombok.Data;

/**
 * Cambios parciales sobre un recorrido: renombrar y/o marcar/desmarcar estrella.
 */
@Data
public class SavedRouteUpdateRequest {
    private String name;
    private Boolean isStarred;
}
