package com.alertamujer.notifications.application;

import com.alertamujer.notifications.application.email.EmailSender.EmailMessage;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Correos que recibe un contacto de emergencia cuando una usuaria activa una alerta y cuando esta termina. */
final class AlertEmailTemplate {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("h:mm a 'del' dd/MM/yyyy");

    private AlertEmailTemplate() {}

    /**
     * @param mapsUrl enlace de respaldo a Google Maps con la última ubicación conocida (puede ser null)
     * @param logoUrl dirección pública del logo (EmailLayout.logoUrl)
     */
    static EmailMessage construir(String para, String nombreContacto, String nombreUsuaria, LocalDateTime inicio,
                                  String seguimientoUrl, String mapsUrl, String numeroEmergencia, String logoUrl) {
        String quien = nombreUsuaria == null || nombreUsuaria.isBlank() ? "Una persona" : nombreUsuaria;
        String hora = inicio == null ? "hace un momento"
                : "a las " + hora(inicio);
        String saludo = nombreContacto == null || nombreContacto.isBlank() ? "Hola" : "Hola, " + nombreContacto;
        String asunto = "🚨 " + quien + " activó una alerta de emergencia";

        String cuerpo = "<p>" + HtmlUtils.htmlEscape(saludo) + ":</p>"
                + "<p><strong>" + HtmlUtils.htmlEscape(quien) + "</strong> activó una alerta de emergencia en AlertaMujer "
                + HtmlUtils.htmlEscape(hora) + " y puede necesitar tu ayuda.</p>"
                + "<p>En el enlace ves dónde está en tiempo real, el recorrido que hace, las grabaciones de la alerta "
                + "y puedes escribirle.</p>"
                + EmailLayout.boton(seguimientoUrl, "Ver dónde está ahora")
                + (mapsUrl == null ? "" : "<p style=\"font-size:14px;text-align:center\">¿La página tarda en cargar? "
                        + "Abre su última ubicación en <a href=\"" + HtmlUtils.htmlEscape(mapsUrl)
                        + "\" style=\"color:" + EmailLayout.VIOLETA + "\">Google Maps</a>.</p>")
                + EmailLayout.destacado("Si crees que está en peligro inmediato, llama al <strong>"
                        + HtmlUtils.htmlEscape(numeroEmergencia) + "</strong>.")
                + EmailLayout.nota("El enlace es personal y deja de funcionar 2 horas después de que termine la alerta. "
                        + "No lo compartas en redes sociales.");
        String html = EmailLayout.pagina(logoUrl, quien + " activó una alerta. Toca para ver dónde está.",
                "🚨 " + quien + " necesita ayuda", cuerpo,
                "Recibes este correo porque " + HtmlUtils.htmlEscape(quien) + " te registró como contacto de emergencia.");

        String texto = saludo + ":\n\n" + quien + " activó una alerta en AlertaMujer " + hora + ".\n"
                + "Ver su ubicación en tiempo real: " + seguimientoUrl + "\n"
                + (mapsUrl == null ? "" : "Última ubicación conocida en Google Maps: " + mapsUrl + "\n")
                + "\nSi crees que está en peligro inmediato, llama al " + numeroEmergencia + ".\n"
                + "El enlace deja de funcionar 2 horas después de que termine la alerta.";
        return new EmailMessage(para, asunto, html, texto);
    }

    /**
     * Aviso de que la alerta terminó, para que el contacto no siga pendiente.
     * @param estado resolved, cancelled o expired
     */
    static EmailMessage construirFin(String para, String nombreContacto, String nombreUsuaria, String estado,
                                     LocalDateTime fin, String numeroEmergencia, String logoUrl) {
        String quien = nombreUsuaria == null || nombreUsuaria.isBlank() ? "una persona" : nombreUsuaria;
        String hora = fin == null ? "hace un momento" : "a las " + hora(fin);
        String saludo = nombreContacto == null || nombreContacto.isBlank() ? "Hola" : "Hola, " + nombreContacto;
        String que = switch (estado == null ? "" : estado) {
            case "resolved" -> "fue marcada como atendida";
            case "cancelled" -> "fue cancelada";
            default -> "terminó automáticamente al superar la duración máxima";
        };
        String asunto = "La alerta de " + quien + " terminó";
        String frase = "La alerta de emergencia de " + quien + " " + que + " " + hora + ".";

        String cuerpo = "<p>" + HtmlUtils.htmlEscape(saludo) + ":</p>"
                + "<p>" + HtmlUtils.htmlEscape(frase) + "</p>"
                + EmailLayout.destacado("Si aún no sabes de ella o crees que sigue en peligro, comunícate con ella "
                        + "o llama al <strong>" + HtmlUtils.htmlEscape(numeroEmergencia) + "</strong>.");
        String html = EmailLayout.pagina(logoUrl, frase, "Alerta terminada", cuerpo,
                "Recibes este correo porque eres contacto de emergencia de " + HtmlUtils.htmlEscape(quien) + ".");
        String texto = saludo + ":\n\n" + frase + "\n\nSi aún no sabes de ella o crees que sigue en peligro, "
                + "comunícate con ella o llama al " + numeroEmergencia + ".";
        return new EmailMessage(para, asunto, html, texto);
    }

    private static String hora(LocalDateTime t) {
        return t.atZone(ZoneId.systemDefault()).withZoneSameInstant(COLOMBIA).format(HORA).replace("AM", "a. m.").replace("PM", "p. m.");
    }
}
