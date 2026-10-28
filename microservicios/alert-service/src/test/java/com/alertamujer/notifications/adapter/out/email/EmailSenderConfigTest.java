package com.alertamujer.notifications.adapter.out.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailSenderConfigTest {

    private final EmailSenderConfig config = new EmailSenderConfig();

    @Test
    void choosesTheConfiguredProvider() {
        assertEquals("resend", config.emailSender("resend", "clave", "AlertaMujer <a@b.co>").provider());
        assertEquals("brevo", config.emailSender("BREVO", "clave", "a@b.co").provider());
    }

    @Test
    void withoutKeyOrSenderDoesNotSend() {
        assertEquals("log", config.emailSender("resend", "", "a@b.co").provider());
        assertEquals("log", config.emailSender("brevo", "clave", " ").provider());
        assertEquals("log", config.emailSender("log", null, null).provider());
    }

    @Test
    void theLogDoesNotShowTheFullEmail() {
        assertEquals("a***@correo.com", LogEmailSender.hide("ana@correo.com"));
    }
}
