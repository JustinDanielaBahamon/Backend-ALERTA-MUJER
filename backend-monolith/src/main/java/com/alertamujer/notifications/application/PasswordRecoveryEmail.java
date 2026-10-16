package com.alertamujer.notifications.application;

import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.notifications.application.email.EmailSender;
import com.alertamujer.notifications.application.email.EmailSender.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

/** Envía el código de recuperación de contraseña que pidió la usuaria desde la web o la app. */
@Service
public class PasswordRecoveryEmail {

    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryEmail.class);

    private final EmailSender emailSender;

    public PasswordRecoveryEmail(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alPedirRecuperacion(PasswordRecoveryRequestedEvent evento) {
        try {
            emailSender.send(construir(evento));
        } catch (EmailSender.EmailException e) {
            log.warn("No se pudo enviar el código de recuperación: {}", e.getMessage());
        }
    }

    static EmailMessage construir(PasswordRecoveryRequestedEvent e) {
        String saludo = e.nombre() == null || e.nombre().isBlank() ? "Hola" : "Hola, " + e.nombre();
        String asunto = "Tu código para recuperar la contraseña de AlertaMujer"; // sin el código: el asunto queda en logs
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;color:#1f2937">
                  <div style="background:#6d28d9;color:#fff;padding:18px 20px;border-radius:12px 12px 0 0">
                    <h1 style="margin:0;font-size:20px">Recuperar contraseña</h1>
                  </div>
                  <div style="border:1px solid #e5e7eb;border-top:0;padding:20px;border-radius:0 0 12px 12px">
                    <p>%s:</p>
                    <p>Pediste recuperar la contraseña de tu cuenta de AlertaMujer. Escribe este código en la web o en la app:</p>
                    <p style="text-align:center;font-size:32px;letter-spacing:10px;font-weight:bold;color:#6d28d9;margin:24px 0">%s</p>
                    <p>El código vence en %d minutos.</p>
                    <p style="font-size:12px;color:#6b7280">Si no fuiste tú, ignora este correo: tu contraseña no cambia.
                       Nunca compartas este código con nadie.</p>
                  </div>
                </div>
                """.formatted(HtmlUtils.htmlEscape(saludo), e.codigo(), e.minutosValidez());
        String texto = saludo + ":\n\nTu código para recuperar la contraseña de AlertaMujer es " + e.codigo()
                + ".\nVence en " + e.minutosValidez() + " minutos.\n\nSi no fuiste tú, ignora este correo.";
        return new EmailMessage(e.email(), asunto, html, texto);
    }
}
