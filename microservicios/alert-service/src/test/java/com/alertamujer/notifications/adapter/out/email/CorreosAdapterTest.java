package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import com.alertamujer.notifications.application.port.out.CorreosPort.CorreoNoEnviadoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorreosAdapterTest {

    private final EmailSender sender = mock(EmailSender.class);
    private final CorreosAdapter correos = new CorreosAdapter(sender, "https://web.app/");

    private EmailMessage enviado() {
        ArgumentCaptor<EmailMessage> m = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(m.capture());
        return m.getValue();
    }

    @Test
    void elAvisoLlevaElLogoElEnlaceYEscapaElNombreDelContacto() {
        correos.avisoDeAlerta("a@b.co", "<script>x</script>", "Ana", null, "https://x/seguimiento/C", null, "123");

        EmailMessage m = enviado();
        assertTrue(m.html().contains("&lt;script&gt;"));
        assertTrue(m.html().contains("src=\"https://web.app/email/logo.png\""));
        assertTrue(m.html().contains("href=\"https://x/seguimiento/C\""));
        assertTrue(m.subject().contains("Ana activó una alerta"));
    }

    @Test
    void elFinDiceComoTermino() {
        correos.finDeAlerta("a@b.co", "Mamá", "Ana", "resolved", LocalDateTime.of(2026, 10, 16, 15, 20), "123");

        EmailMessage m = enviado();
        assertTrue(m.subject().contains("terminó"));
        assertTrue(m.text().contains("atendida"));
    }

    @Test
    void elCodigoNoVaEnElAsunto() {
        correos.codigoRecuperacion("a@b.co", "Ana", "123456", 15);

        EmailMessage m = enviado();
        assertFalse(m.subject().contains("123456"));
        assertTrue(m.text().contains("123456"));
    }

    @Test
    void unRechazoDelProveedorSeTraduceAlPuerto() {
        doThrow(new EmailSender.EmailException("Rechazado")).when(sender).send(any());

        CorreoNoEnviadoException e = assertThrows(CorreoNoEnviadoException.class,
                () -> correos.codigoRecuperacion("a@b.co", "Ana", "123456", 15));
        assertEquals("Rechazado", e.getMessage());
    }
}
