package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import com.alertamujer.notifications.application.port.out.EmailsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Adaptador de salida: arma cada correo con la plantilla de la marca y lo envía con el proveedor configurado. */
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
                              String trackingUrl, String mapsUrl, String emergencyNumber) {
        send(AlertEmailTemplate.build(to, contactName, userName, start, trackingUrl, mapsUrl,
                emergencyNumber, logoUrl));
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
