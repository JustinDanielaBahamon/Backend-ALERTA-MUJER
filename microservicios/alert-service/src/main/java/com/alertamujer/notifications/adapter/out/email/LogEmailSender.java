package com.alertamujer.notifications.adapter.out.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** No provider configured: sends nothing and leaves a trace in the log (local development). */
public class LogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void send(EmailMessage m) {
        // The link is not logged: the tracking code is a credential
        log.info("Correo NO enviado (EMAIL_PROVIDER sin configurar) para {} · asunto: {}", hide(m.to()), m.subject());
        throw new EmailException("No hay proveedor de correo configurado");
    }

    @Override
    public String provider() {
        return "log";
    }

    static String hide(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        return at <= 1 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }
}
