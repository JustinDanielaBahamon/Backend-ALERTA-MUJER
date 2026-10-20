package com.alertamujer.settings.domain.model;

/** Cambios de la administración en la configuración. null = sin cambio; minAppVersion "" quita la versión mínima. */
public record CambiosAjustes(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                             Integer maxEmergencyContacts, String minAppVersion) {
}
