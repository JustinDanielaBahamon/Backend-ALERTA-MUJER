package com.alertamujer.devices.application.port.in;

import java.util.List;

/** Queries asked by the other modules and Alert Service. */
public interface QueryDevicesUseCase {

    /** Push tokens of the profile owner (user_profile.id). */
    List<String> profileTokens(Long profile);

    /** Push tokens of the person (users.id); empty if she has no account. */
    List<String> userTokens(Long userId);

    /** Phones only (no web browsers): to request the GPS location. */
    List<String> phoneTokens(Long userId);

    boolean isTheNewestPhone(Long userId, Long deviceId);

    long count();
}
