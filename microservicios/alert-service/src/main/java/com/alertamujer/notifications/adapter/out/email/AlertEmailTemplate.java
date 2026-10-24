package com.alertamujer.notifications.adapter.out.email;

import com.alertamujer.notifications.adapter.out.email.EmailSender.EmailMessage;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Emails an emergency contact receives when a user activates an alert and when it ends. */
final class AlertEmailTemplate {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a 'del' dd/MM/yyyy");

    private AlertEmailTemplate() {}

    /**
     * @param mapsUrl fallback Google Maps link with the last known location (may be null)
     * @param logoUrl public address of the logo (EmailLayout.logoUrl)
     */
    static EmailMessage build(String to, String contactName, String userName, LocalDateTime start,
                                  String trackingUrl, String mapsUrl, String emergencyNumber, String logoUrl,
                                  String openedPixelUrl) {
        String who = userName == null || userName.isBlank() ? "Una persona" : userName;
        String time = start == null ? "hace un momento"
                : "a las " + time(start);
        String greeting = contactName == null || contactName.isBlank() ? "Hola" : "Hola, " + contactName;
        String subject = "🚨 " + who + " activó una alerta de emergencia";

        String body = "<p>" + HtmlUtils.htmlEscape(greeting) + ":</p>"
                + "<p><strong>" + HtmlUtils.htmlEscape(who) + "</strong> activó una alerta de emergencia en AlertaMujer "
                + HtmlUtils.htmlEscape(time) + " y puede necesitar tu ayuda.</p>"
                + "<p>En el enlace ves dónde está en tiempo real, el recorrido que hace, las grabaciones de la alerta "
                + "y puedes escribirle.</p>"
                + EmailLayout.button(trackingUrl, "Ver dónde está ahora")
                + (mapsUrl == null ? "" : "<p style=\"font-size:14px;text-align:center\">¿La página tarda en cargar? "
                        + "Abre su última ubicación en <a href=\"" + HtmlUtils.htmlEscape(mapsUrl)
                        + "\" style=\"color:" + EmailLayout.VIOLET + "\">Google Maps</a>.</p>")
                + EmailLayout.highlighted("Si crees que está en peligro inmediato, llama al <strong>"
                        + HtmlUtils.htmlEscape(emergencyNumber) + "</strong>.")
                + EmailLayout.note("El enlace es personal y deja de funcionar 2 horas después de que termine la alerta. "
                        + "No lo compartas en redes sociales.")
                // Tells the user the email was opened (it does not identify anything else)
                + (openedPixelUrl == null ? "" : "<img src=\"" + HtmlUtils.htmlEscape(openedPixelUrl)
                        + "\" width=\"1\" height=\"1\" alt=\"\" style=\"display:block;border:0\" />");
        String html = EmailLayout.page(logoUrl, who + " activó una alerta. Toca para ver dónde está.",
                "🚨 " + who + " necesita ayuda", body,
                "Recibes este correo porque " + HtmlUtils.htmlEscape(who) + " te registró como contacto de emergencia.");

        String text = greeting + ":\n\n" + who + " activó una alerta en AlertaMujer " + time + ".\n"
                + "Ver su ubicación en tiempo real: " + trackingUrl + "\n"
                + (mapsUrl == null ? "" : "Última ubicación conocida en Google Maps: " + mapsUrl + "\n")
                + "\nSi crees que está en peligro inmediato, llama al " + emergencyNumber + ".\n"
                + "El enlace deja de funcionar 2 horas después de que termine la alerta.";
        return new EmailMessage(to, subject, html, text);
    }

    /**
     * Second notice when nobody opened the tracking a few minutes after the alert started.
     * @param reason why the server activated it (trips), or null
     */
    static EmailMessage buildReminder(String to, String contactName, String userName, LocalDateTime start,
                                      String trackingUrl, String reason, String emergencyNumber, String logoUrl) {
        String who = userName == null || userName.isBlank() ? "Una persona" : userName;
        String time = start == null ? "hace unos minutos" : "a las " + time(start);
        String greeting = contactName == null || contactName.isBlank() ? "Hola" : "Hola, " + contactName;
        String subject = "⚠️ La alerta de " + who + " sigue activa y nadie la ha visto";

        String body = "<p>" + HtmlUtils.htmlEscape(greeting) + ":</p>"
                + "<p>La alerta de emergencia que <strong>" + HtmlUtils.htmlEscape(who) + "</strong> activó "
                + HtmlUtils.htmlEscape(time) + " <strong>sigue activa</strong> y ninguno de sus contactos ha abierto "
                + "el seguimiento todavía.</p>"
                + (reason == null || reason.isBlank() ? "" : "<p>" + HtmlUtils.htmlEscape(reason) + "</p>")
                + EmailLayout.button(trackingUrl, "Ver dónde está ahora")
                + EmailLayout.highlighted("Si no logras comunicarte con ella y crees que está en peligro, llama al <strong>"
                        + HtmlUtils.htmlEscape(emergencyNumber) + "</strong>.")
                + EmailLayout.note("El enlace es personal y deja de funcionar 2 horas después de que termine la alerta. "
                        + "No lo compartas en redes sociales.");
        String html = EmailLayout.page(logoUrl, "La alerta de " + who + " sigue activa. Toca para ver dónde está.",
                "⚠️ " + who + " sigue necesitando ayuda", body,
                "Recibes este correo porque " + HtmlUtils.htmlEscape(who) + " te registró como contacto de emergencia.");

        String text = greeting + ":\n\nLa alerta que " + who + " activó " + time + " sigue activa y nadie ha abierto "
                + "el seguimiento.\n"
                + (reason == null || reason.isBlank() ? "" : reason + "\n")
                + "Ver su ubicación en tiempo real: " + trackingUrl + "\n"
                + "\nSi no logras comunicarte con ella y crees que está en peligro, llama al " + emergencyNumber + ".";
        return new EmailMessage(to, subject, html, text);
    }

    /**
     * Notice that the alert ended, so the contact stops waiting.
     * @param status resolved, cancelled or expired
     */
    static EmailMessage buildEnd(String to, String contactName, String userName, String status,
                                     LocalDateTime end, String emergencyNumber, String logoUrl) {
        String who = userName == null || userName.isBlank() ? "una persona" : userName;
        String time = end == null ? "hace un momento" : "a las " + time(end);
        String greeting = contactName == null || contactName.isBlank() ? "Hola" : "Hola, " + contactName;
        String what = switch (status == null ? "" : status) {
            case "resolved" -> "fue marcada como atendida";
            case "cancelled" -> "fue cancelada";
            default -> "terminó automáticamente al superar la duración máxima";
        };
        String subject = "La alerta de " + who + " terminó";
        String sentence = "La alerta de emergencia de " + who + " " + what + " " + time + ".";

        String body = "<p>" + HtmlUtils.htmlEscape(greeting) + ":</p>"
                + "<p>" + HtmlUtils.htmlEscape(sentence) + "</p>"
                + EmailLayout.highlighted("Si aún no sabes de ella o crees que sigue en peligro, comunícate con ella "
                        + "o llama al <strong>" + HtmlUtils.htmlEscape(emergencyNumber) + "</strong>.");
        String html = EmailLayout.page(logoUrl, sentence, "Alerta terminada", body,
                "Recibes este correo porque eres contacto de emergencia de " + HtmlUtils.htmlEscape(who) + ".");
        String text = greeting + ":\n\n" + sentence + "\n\nSi aún no sabes de ella o crees que sigue en peligro, "
                + "comunícate con ella o llama al " + emergencyNumber + ".";
        return new EmailMessage(to, subject, html, text);
    }

    private static String time(LocalDateTime t) {
        return t.atZone(ZoneId.systemDefault()).withZoneSameInstant(COLOMBIA).format(TIME).replace("AM", "a. m.").replace("PM", "p. m.");
    }
}
