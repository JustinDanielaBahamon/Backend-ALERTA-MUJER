package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import org.springframework.web.util.HtmlUtils;

/** Email with the password recovery code the user requested from the web or the app. */
final class RecoveryCodeTemplate {

    private RecoveryCodeTemplate() {}

    static EmailMessage build(String to, String name, String code, int validityMinutes, String logoUrl) {
        String greeting = name == null || name.isBlank() ? "Hola" : "Hola, " + name;
        String subject = "Tu código para recuperar la contraseña de AlertaMujer"; // without the code: the subject ends up in logs
        String body = "<p>" + HtmlUtils.htmlEscape(greeting) + ":</p>"
                + "<p>Pediste recuperar la contraseña de tu cuenta de AlertaMujer. Escribe este código en la web o en la app:</p>"
                + "<p style=\"text-align:center;font-size:34px;letter-spacing:10px;font-weight:bold;color:"
                + EmailLayout.VIOLET + ";background:" + EmailLayout.LIGHT_VIOLET
                + ";border-radius:14px;padding:16px 0;margin:24px 0\">" + HtmlUtils.htmlEscape(code) + "</p>"
                + "<p>El código vence en " + validityMinutes + " minutos.</p>"
                + EmailLayout.note("Si no fuiste tú, ignora este correo: tu contraseña no cambia. "
                        + "Nunca compartas este código con nadie.");
        String html = EmailLayout.page(logoUrl, "Tu código vence en " + validityMinutes + " minutos.",
                "Recuperar contraseña", body, "Recibes este correo porque se pidió recuperar la contraseña de tu cuenta.");
        String text = greeting + ":\n\nTu código para recuperar la contraseña de AlertaMujer es " + code
                + ".\nVence en " + validityMinutes + " minutos.\n\nSi no fuiste tú, ignora este correo.";
        return new EmailMessage(to, subject, html, text);
    }
}
