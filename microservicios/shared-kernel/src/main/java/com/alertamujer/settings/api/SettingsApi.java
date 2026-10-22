package com.alertamujer.settings.api;

/** System parameters other modules need. */
public interface SettingsApi {

    /** Minutes after which an active alert is marked as expired (0 = not configured). */
    int maxAlertDurationMinutes();

    /** Emergency number offered to users and their contacts (123 in Colombia). */
    String emergencyNumber();

    /** Maximum emergency contacts per user (5 if not configured). */
    int maxContacts();
}
