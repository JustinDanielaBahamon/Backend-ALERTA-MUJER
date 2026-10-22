package com.alertamujer.settings.adapter.in.web.dto;

import com.alertamujer.settings.domain.model.SettingsChanges;

/** minAppVersion vacío ("") quita la versión mínima. */
public record SettingsRequest(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                              Integer maxEmergencyContacts, String minAppVersion) {

    public SettingsChanges toChanges() {
        return new SettingsChanges(sosMaxDurationMinutes, emergencyPhoneNumber, supportEmail, maxEmergencyContacts, minAppVersion);
    }
}
