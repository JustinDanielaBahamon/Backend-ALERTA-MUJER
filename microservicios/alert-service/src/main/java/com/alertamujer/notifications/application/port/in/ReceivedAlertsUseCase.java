package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alerts the user received as another person's emergency contact (her email was
 * notified). From the web panel she sees which are active and opens their tracking.
 */
public interface ReceivedAlertsUseCase {

    /** Those of the last 30 days, newest to oldest. */
    List<ReceivedAlert> received(Long userId, LocalDateTime now);

    /** Tracking code of a received alert, only if the tracking is still valid. */
    String trackingLink(Long userId, Long alertId, LocalDateTime now);

    /** What the contact sees: only the first name of whoever activated the alert. */
    record ReceivedAlert(Long alertId, String firstName, String status, boolean active,
                         LocalDateTime startedAt, LocalDateTime endedAt, boolean trackingAvailable) {}
}
