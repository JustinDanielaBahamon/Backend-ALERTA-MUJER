package com.alertamujer.settings.application.port.in;

import com.alertamujer.settings.domain.model.SystemConfiguration;

/** What the apps and the other modules and microservices read. */
public interface QuerySettingsUseCase {

    /** For the apps of any user: emergency number, support, duration and minimum version. */
    SystemConfiguration publicPaths();

    /** 0 if not configured (no alert expires). */
    int maxAlertDurationMinutes();

    /** "123" if not configured. */
    String emergencyNumber();

    /** 5 if not configured. */
    int maxContacts();
}
