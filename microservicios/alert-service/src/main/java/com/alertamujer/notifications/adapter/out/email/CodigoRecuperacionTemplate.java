package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import org.springframework.web.util.HtmlUtils;

/** Correo con el código de recuperación de contraseña que pidió la usuaria desde la web o la app. */
final class CodigoRecuperacionTemplate {

    private CodigoRecuperacionTemplate() {}

    static EmailMessage construir(String para, String nombre, String codigo, int minutosValidez, String logoUrl) {
        String saludo = nombre == null || nombre.isBlank() ? "Hola" : "Hola, " + nombre;
        String asunto = "Tu código para recuperar la contraseña de AlertaMujer"; // sin el código: el asunto queda en logs
        String cuerpo = "<p>" + HtmlUtils.htmlEscape(saludo) + ":</p>"
                + "<p>Pediste recuperar la contraseña de tu cuenta de AlertaMujer. Escribe este código en la web o en la app:</p>"
                + "<p style=\"text-align:center;font-size:34px;letter-spacing:10px;font-weight:bold;color:"
                + EmailLayout.VIOLETA + ";background:" + EmailLayout.VIOLETA_CLARO
                + ";border-radius:14px;padding:16px 0;margin:24px 0\">" + HtmlUtils.htmlEscape(codigo) + "</p>"
                + "<p>El código vence en " + minutosValidez + " minutos.</p>"
                + EmailLayout.nota("Si no fuiste tú, ignora este correo: tu contraseña no cambia. "
                        + "Nunca compartas este código con nadie.");
        String html = EmailLayout.pagina(logoUrl, "Tu código vence en " + minutosValidez + " minutos.",
                "Recuperar contraseña", cuerpo, "Recibes este correo porque se pidió recuperar la contraseña de tu cuenta.");
        String texto = saludo + ":\n\nTu código para recuperar la contraseña de AlertaMujer es " + codigo
                + ".\nVence en " + minutosValidez + " minutos.\n\nSi no fuiste tú, ignora este correo.";
        return new EmailMessage(para, asunto, html, texto);
    }
}
