package com.alertamujer.settings.domain.model;

/** Configuration changes by the administration. null = no change; minAppVersion "" removes the minimum version. */
public record SettingsChanges(Integer sosMaxDurationMinutes, String emergencyPhoneNumber, String supportEmail,
                             Integer maxEmergencyContacts, String minAppVersion) {
}
