package com.alertamujer.notifications.application;

import com.alertamujer.notifications.application.email.EmailSender.EmailMessage;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Correo que recibe un contacto de emergencia cuando una usuaria activa una alerta. */
final class AlertEmailTemplate {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("h:mm a 'del' dd/MM/yyyy");

    private AlertEmailTemplate() {}

    /**
     * @param mapsUrl enlace de respaldo a Google Maps con la última ubicación conocida (puede ser null)
     */
    static EmailMessage construir(String para, String nombreContacto, String nombreUsuaria, LocalDateTime inicio,
                                  String seguimientoUrl, String mapsUrl, String numeroEmergencia) {
        String quien = nombreUsuaria == null || nombreUsuaria.isBlank() ? "Una persona" : nombreUsuaria;
        String hora = inicio == null ? "hace un momento"
                : "a las " + inicio.atZone(ZoneId.systemDefault()).withZoneSameInstant(COLOMBIA).format(HORA).replace("AM", "a. m.").replace("PM", "p. m.");
        String saludo = nombreContacto == null || nombreContacto.isBlank() ? "Hola" : "Hola, " + nombreContacto;
        String asunto = "🚨 " + quien + " activó una alerta de emergencia";

        String e = HtmlUtils.htmlEscape(quien);
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;color:#1f2937">
                  <div style="background:#be123c;color:#fff;padding:18px 20px;border-radius:12px 12px 0 0">
                    <h1 style="margin:0;font-size:20px">🚨 Alerta de emergencia</h1>
                  </div>
                  <div style="border:1px solid #e5e7eb;border-top:0;padding:20px;border-radius:0 0 12px 12px">
                    <p>%s:</p>
                    <p><strong>%s</strong> activó una alerta en AlertaMujer %s. Recibes este correo porque te registró
                       como contacto de emergencia.</p>
                    <p style="text-align:center;margin:26px 0">
                      <a href="%s" style="background:#7c3aed;color:#fff;padding:14px 22px;border-radius:10px;
                         text-decoration:none;font-weight:bold;display:inline-block">Ver su ubicación en tiempo real</a>
                    </p>
                    %s
                    <p style="background:#fef2f2;border-radius:8px;padding:12px">Si crees que está en peligro inmediato,
                       llama al <strong>%s</strong>.</p>
                    <p style="font-size:12px;color:#6b7280">El enlace es personal y deja de funcionar 2 horas después de
                       que termine la alerta. No lo compartas en redes sociales.</p>
                  </div>
                </div>
                """.formatted(HtmlUtils.htmlEscape(saludo), e, HtmlUtils.htmlEscape(hora), HtmlUtils.htmlEscape(seguimientoUrl),
                mapsUrl == null ? "" : "<p style=\"font-size:14px\">Si la página tarda en cargar, abre la última ubicación "
                        + "conocida en <a href=\"" + HtmlUtils.htmlEscape(mapsUrl) + "\">Google Maps</a>.</p>",
                HtmlUtils.htmlEscape(numeroEmergencia));

        String texto = saludo + ":\n\n" + quien + " activó una alerta en AlertaMujer " + hora + ".\n"
                + "Ver su ubicación en tiempo real: " + seguimientoUrl + "\n"
                + (mapsUrl == null ? "" : "Última ubicación conocida en Google Maps: " + mapsUrl + "\n")
                + "\nSi crees que está en peligro inmediato, llama al " + numeroEmergencia + ".\n"
                + "El enlace deja de funcionar 2 horas después de que termine la alerta.";
        return new EmailMessage(para, asunto, html, texto);
    }
}
