package com.alertamujer.settings.adapter.in.web.dto;

import com.alertamujer.settings.domain.model.SystemConfiguration;

import java.time.LocalDateTime;

/** Configuration for the admin panel. */
public record SettingsView(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                           Integer maxEmergencyContacts, String minAppVersion, LocalDateTime updatedAt) {

    public static SettingsView of(SystemConfiguration c) {
        return new SettingsView(c.getSosMaxDurationMinutes(), c.getEmergencyPhoneNumber(), c.getAdminEmail(),
                c.getMaxEmergencyContacts(), c.getMinAppVersion(), c.lastUpdate());
    }
}
