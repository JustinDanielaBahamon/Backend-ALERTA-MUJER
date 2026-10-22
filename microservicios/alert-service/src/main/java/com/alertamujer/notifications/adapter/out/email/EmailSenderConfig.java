package com.alertamujer.notifications.adapter.out.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Elige el proveedor de correo: notifications.email.provider = resend | brevo | log. */
@Configuration
public class EmailSenderConfig {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderConfig.class);

    @Bean
    public EmailSender emailSender(@Value("${notifications.email.provider:log}") String provider,
                                   @Value("${notifications.email.api-key:}") String apiKey,
                                   @Value("${notifications.email.from:}") String from) {
        String p = provider == null ? "log" : provider.trim().toLowerCase();
        if (!p.equals("log") && (apiKey == null || apiKey.isBlank() || from == null || from.isBlank())) {
            log.warn("EMAIL_PROVIDER={} sin EMAIL_API_KEY o EMAIL_FROM: los correos no se enviarán", p);
            return new LogEmailSender();
        }
        return switch (p) {
            case "resend" -> new ResendEmailSender(apiKey.trim(), from.trim());
            case "brevo" -> new BrevoEmailSender(apiKey.trim(), from.trim());
            default -> new LogEmailSender();
        };
    }
}
