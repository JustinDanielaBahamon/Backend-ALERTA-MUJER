package com.alertamujer.settings.adapter.in.web.dto;

import com.alertamujer.settings.domain.model.SystemConfiguration;

/** Lo que necesitan las apps de cualquier usuaria. */
public record PublicSettings(String emergencyPhoneNumber, String supportEmail, Integer sosMaxDurationMinutes,
                             Integer maxEmergencyContacts, String minAppVersion) {

    public static PublicSettings of(SystemConfiguration c) {
        return new PublicSettings(c.getEmergencyPhoneNumber(), c.getAdminEmail(), c.getSosMaxDurationMinutes(),
                c.getMaxEmergencyContacts(), c.getMinAppVersion());
    }
}
