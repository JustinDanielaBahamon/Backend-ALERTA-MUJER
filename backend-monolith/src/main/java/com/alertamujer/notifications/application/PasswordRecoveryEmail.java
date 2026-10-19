package com.alertamujer.notifications.application;

import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.notifications.application.email.EmailSender;
import com.alertamujer.notifications.application.email.EmailSender.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final String logoUrl;

    public PasswordRecoveryEmail(EmailSender emailSender,
                                 @Value("${notifications.web-base-url:http://localhost:4200}") String webBaseUrl) {
        this.emailSender = emailSender;
        this.logoUrl = EmailLayout.logoUrl(webBaseUrl);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alPedirRecuperacion(PasswordRecoveryRequestedEvent evento) {
        try {
            emailSender.send(construir(evento, logoUrl));
        } catch (EmailSender.EmailException e) {
            log.warn("No se pudo enviar el código de recuperación: {}", e.getMessage());
        }
    }

    static EmailMessage construir(PasswordRecoveryRequestedEvent e, String logoUrl) {
        String saludo = e.nombre() == null || e.nombre().isBlank() ? "Hola" : "Hola, " + e.nombre();
        String asunto = "Tu código para recuperar la contraseña de AlertaMujer"; // sin el código: el asunto queda en logs
        String cuerpo = "<p>" + HtmlUtils.htmlEscape(saludo) + ":</p>"
                + "<p>Pediste recuperar la contraseña de tu cuenta de AlertaMujer. Escribe este código en la web o en la app:</p>"
                + "<p style=\"text-align:center;font-size:34px;letter-spacing:10px;font-weight:bold;color:"
                + EmailLayout.VIOLETA + ";background:" + EmailLayout.VIOLETA_CLARO
                + ";border-radius:14px;padding:16px 0;margin:24px 0\">" + HtmlUtils.htmlEscape(e.codigo()) + "</p>"
                + "<p>El código vence en " + e.minutosValidez() + " minutos.</p>"
                + EmailLayout.nota("Si no fuiste tú, ignora este correo: tu contraseña no cambia. "
                        + "Nunca compartas este código con nadie.");
        String html = EmailLayout.pagina(logoUrl, "Tu código vence en " + e.minutosValidez() + " minutos.",
                "Recuperar contraseña", cuerpo, "Recibes este correo porque se pidió recuperar la contraseña de tu cuenta.");
        String texto = saludo + ":\n\nTu código para recuperar la contraseña de AlertaMujer es " + e.codigo()
                + ".\nVence en " + e.minutosValidez() + " minutos.\n\nSi no fuiste tú, ignora este correo.";
        return new EmailMessage(e.email(), asunto, html, texto);
    }
}
