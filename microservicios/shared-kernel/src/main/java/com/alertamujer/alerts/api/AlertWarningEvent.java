package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/**
 * Something the contacts must know about an alert that is still active:
 * <ul>
 *   <li>{@code unanswered}: the check-in time passed and she did not say she is still in danger;</li>
 *   <li>{@code signal_lost}: her phone stopped sending its location ({@code at} = last signal);</li>
 *   <li>{@code duress}: she "cancelled" with her duress PIN, it may be under pressure.</li>
 * </ul>
 */
public record AlertWarningEvent(Long alertId, Long userProfileId, String kind, LocalDateTime at) {

    public static final String UNANSWERED = "unanswered";
    public static final String SIGNAL_LOST = "signal_lost";
    public static final String DURESS = "duress";
}
