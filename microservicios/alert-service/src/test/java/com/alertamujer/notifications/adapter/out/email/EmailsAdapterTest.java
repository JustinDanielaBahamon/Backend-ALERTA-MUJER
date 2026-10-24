package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import com.alertamujer.notifications.application.port.out.EmailsPort.EmailNotSentException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailsAdapterTest {

    private final EmailSender sender = mock(EmailSender.class);
    private final EmailsAdapter emails = new EmailsAdapter(sender, "https://web.app/");

    private EmailMessage sent() {
        ArgumentCaptor<EmailMessage> m = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(m.capture());
        return m.getValue();
    }

    @Test
    void theNoticeHasTheLogoTheLinkAndEscapesTheContactName() {
        emails.alertNotice("a@b.co", "<script>x</script>", "Ana", null, "https://x/seguimiento/C", null, "123", null);

        EmailMessage m = sent();
        assertTrue(m.html().contains("&lt;script&gt;"));
        assertTrue(m.html().contains("src=\"https://web.app/email/logo.png\""));
        assertTrue(m.html().contains("href=\"https://x/seguimiento/C\""));
        assertTrue(m.subject().contains("Ana activó una alerta"));
    }

    @Test
    void theEndSaysHowItEnded() {
        emails.alertEnd("a@b.co", "Mamá", "Ana", "resolved", LocalDateTime.of(2026, 10, 16, 15, 20), "123");

        EmailMessage m = sent();
        assertTrue(m.subject().contains("terminó"));
        assertTrue(m.text().contains("atendida"));
    }

    @Test
    void theCodeIsNotInTheSubject() {
        emails.recoveryCode("a@b.co", "Ana", "123456", 15);

        EmailMessage m = sent();
        assertFalse(m.subject().contains("123456"));
        assertTrue(m.text().contains("123456"));
    }

    @Test
    void aProviderRejectionIsTranslatedToThePort() {
        doThrow(new EmailSender.EmailException("Rechazado")).when(sender).send(any());

        EmailNotSentException e = assertThrows(EmailNotSentException.class,
                () -> emails.recoveryCode("a@b.co", "Ana", "123456", 15));
        assertEquals("Rechazado", e.getMessage());
    }
}
