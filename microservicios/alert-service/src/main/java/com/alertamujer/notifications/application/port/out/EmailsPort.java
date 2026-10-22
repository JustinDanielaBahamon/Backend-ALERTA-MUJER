package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/**
 * Output port: the app emails. The adapter builds the HTML with the brand template and
 * sends it with the configured provider (Brevo, Resend or log only).
 */
public interface EmailsPort {

    /** Notice to the contact that the user activated an alert. mapsUrl may be null. */
    void alertNotice(String to, String contactName, String userName, LocalDateTime start,
                       String trackingUrl, String mapsUrl, String emergencyNumber);

    /** Notice that the alert ended. status: resolved, cancelled or expired. */
    void alertEnd(String to, String contactName, String userName, String status, LocalDateTime end,
                     String emergencyNumber);

    void recoveryCode(String to, String name, String code, int validityMinutes);

    /** The provider rejected the email or no provider is configured. */
    class EmailNotSentException extends RuntimeException {
        public EmailNotSentException(String reason) {
            super(reason);
        }
    }
}
