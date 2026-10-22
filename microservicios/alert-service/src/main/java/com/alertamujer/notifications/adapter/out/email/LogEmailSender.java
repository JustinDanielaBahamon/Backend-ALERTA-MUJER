package com.alertamujer.notifications.adapter.out.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sin proveedor configurado: no envía nada y deja constancia en el log (desarrollo local). */
public class LogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void send(EmailMessage m) {
        // No se registra el enlace: el código de seguimiento es una credencial
        log.info("Correo NO enviado (EMAIL_PROVIDER sin configurar) para {} · asunto: {}", ocultar(m.to()), m.subject());
        throw new EmailException("No hay proveedor de correo configurado");
    }

    @Override
    public String provider() {
        return "log";
    }

    static String ocultar(String email) {
        int arroba = email == null ? -1 : email.indexOf('@');
        return arroba <= 1 ? "***" : email.charAt(0) + "***" + email.substring(arroba);
    }
}
