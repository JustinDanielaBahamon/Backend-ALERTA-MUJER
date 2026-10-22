package com.alertamujer.alerts.application.port.out;

/** Output port: the configuration defined by the administration (Auth Service). */
public interface SettingsPort {

    /** Minutes after which an active alert is marked as expired (0 = not configured). */
    int maxAlertDurationMinutes();

    int maxContacts();
}
