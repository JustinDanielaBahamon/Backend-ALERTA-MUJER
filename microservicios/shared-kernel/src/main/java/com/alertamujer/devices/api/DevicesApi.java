package com.alertamujer.devices.api;

import java.util.List;

/** Queries of the devices module for the other modules. */
public interface DevicesApi {

    /** FCM tokens of the devices of the user who owns the profile (user_profile.id). */
    List<String> pushTokensOfProfile(Long userProfileId);

    /** FCM tokens of a person's devices (users.id); empty if she has no account or devices. */
    List<String> pushTokensOfUser(Long userId);

    /** Push tokens of phones only (no web browsers): to request the GPS location. */
    List<String> phonePushTokensOfUser(Long userId);

    /**
     * true if the device is the phone the user used most recently (or if unknown):
     * with several phones, the day-to-day location is taken only from that one.
     */
    boolean isMostRecentPhone(Long userId, Long deviceId);

    /** Number of registered devices (panel stats). */
    long countDevices();
}
