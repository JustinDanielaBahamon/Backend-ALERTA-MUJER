package com.alertamujer.alerts.application.port.out;

import java.util.List;

/** Output port: the user's phones (Auth Service). */
public interface PhonesPort {

    /** true if the device is the phone the user used most recently (or if unknown). */
    boolean isTheNewest(Long userId, Long deviceId);

    /** Push tokens of phones only (no web browsers). */
    List<String> phoneTokens(Long userId);
}
