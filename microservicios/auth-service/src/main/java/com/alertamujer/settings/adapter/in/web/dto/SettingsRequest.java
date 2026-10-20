package com.alertamujer.settings.adapter.in.web.dto;

import com.alertamujer.settings.domain.model.CambiosAjustes;

/** minAppVersion vacío ("") quita la versión mínima. */
public record SettingsRequest(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                              Integer maxEmergencyContacts, String minAppVersion) {

    public CambiosAjustes aCambios() {
        return new CambiosAjustes(sosMaxDurationMinutes, emergencyPhoneNumber, supportEmail, maxEmergencyContacts, minAppVersion);
    }
}
