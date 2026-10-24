package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import com.alertamujer.notifications.application.port.out.EmailsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Output adapter: builds each email with the brand template and sends it with the configured provider. */
@Component
class EmailsAdapter implements EmailsPort {

    private final EmailSender emailSender;
    private final String logoUrl;

    EmailsAdapter(EmailSender emailSender, @Value("${notifications.web-base-url:http://localhost:4200}") String webBaseUrl) {
        this.emailSender = emailSender;
        this.logoUrl = EmailLayout.logoUrl(webBaseUrl);
    }

    @Override
    public void alertNotice(String to, String contactName, String userName, LocalDateTime start,
                              String trackingUrl, String mapsUrl, String emergencyNumber, String openedPixelUrl) {
        send(AlertEmailTemplate.build(to, contactName, userName, start, trackingUrl, mapsUrl,
                emergencyNumber, logoUrl, openedPixelUrl));
    }

    @Override
    public void alertReminder(String to, String contactName, String userName, LocalDateTime start,
                              String trackingUrl, String reason, String emergencyNumber) {
        send(AlertEmailTemplate.buildReminder(to, contactName, userName, start, trackingUrl, reason,
                emergencyNumber, logoUrl));
    }

    @Override
    public void alertWarning(String to, String contactName, String userName, String kind, LocalDateTime at,
                             String trackingUrl, String emergencyNumber) {
        send(AlertEmailTemplate.buildWarning(to, contactName, userName, kind, at, trackingUrl, emergencyNumber, logoUrl));
    }

    @Override
    public void alertEnd(String to, String contactName, String userName, String status, LocalDateTime end,
                            String emergencyNumber) {
        send(AlertEmailTemplate.buildEnd(to, contactName, userName, status, end, emergencyNumber, logoUrl));
    }

    @Override
    public void recoveryCode(String to, String name, String code, int validityMinutes) {
        send(RecoveryCodeTemplate.build(to, name, code, validityMinutes, logoUrl));
    }

    private void send(EmailMessage message) {
        try {
            emailSender.send(message);
        } catch (EmailSender.EmailException e) {
            throw new EmailNotSentException(e.getMessage());
        }
    }
}
