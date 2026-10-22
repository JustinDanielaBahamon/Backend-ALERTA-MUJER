package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;

/**
 * Notifies the emergency contacts: when the alert is activated, an email with the tracking link and a
 * high-priority push to those who have the app; when it ends, a notice so they stop waiting.
 */
public interface NotifyContactsUseCase {

    /** Stores the result of each delivery in the alert (sent / failed). */
    void alertActivated(Long alertId, Long userProfile, LocalDateTime start);

    /** Courtesy notice: does not change the delivery status and a failure is only logged. */
    void alertEnded(Long alertId, Long userProfile, String status, LocalDateTime end);
}
