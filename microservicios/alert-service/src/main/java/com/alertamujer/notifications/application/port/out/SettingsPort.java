package com.alertamujer.notifications.application.port.out;

/** Output port to settings (Auth Service over HTTP). */
public interface SettingsPort {

    /** Emergency number offered to the contacts (123 in Colombia). */
    String emergencyNumber();
}
