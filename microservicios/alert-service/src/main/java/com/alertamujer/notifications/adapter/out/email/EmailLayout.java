package com.alertamujer.notifications.adapter.out.email;

import org.springframework.web.util.HtmlUtils;

/**
 * Plantilla común de los correos: encabezado violeta con el logo de la app, tarjeta blanca y pie.
 * Hecha con tablas y estilos en línea porque Gmail y Outlook ignoran CSS externo y flexbox. El logo
 * se carga desde la web pública (Gmail no muestra imágenes incrustadas como data URI).
 */
final class EmailLayout {

    static final String VIOLETA = "#7B1DB2";
    static final String VIOLETA_OSCURO = "#2B0C3B";
    static final String VIOLETA_CLARO = "#F4EEFB";
    private static final String TEXTO = "#2D1B3D";
    private static final String TEXTO_SUAVE = "#6E5A80";

    private EmailLayout() {}

    /** Dirección pública del logo (web/public/email/logo.png). */
    static String logoUrl(String webBaseUrl) {
        return webBaseUrl.replaceAll("/+$", "") + "/email/logo.png";
    }

    /**
     * @param resumen texto corto que Gmail muestra junto al asunto en la bandeja (no se ve en el correo)
     * @param cuerpo  HTML ya escapado
     * @param pie     HTML ya escapado con el motivo por el que llega el correo
     */
    static String pagina(String logoUrl, String resumen, String titulo, String body, String pie) {
        return """
                <!doctype html>
                <html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:%5$s">
                <div style="display:none;max-height:0;overflow:hidden;opacity:0">%1$s</div>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%5$s;padding:24px 12px">
                  <tr><td align="center">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                           style="max-width:560px;font-family:Arial,Helvetica,sans-serif;color:%7$s">
                      <tr><td style="background:%6$s;background-image:linear-gradient(135deg,%6$s,%8$s);
                                     border-radius:18px 18px 0 0;padding:26px 24px;text-align:center">
                        <img src="%2$s" width="72" height="72" alt="AlertaMujer"
                             style="display:block;margin:0 auto 12px;border-radius:50%%;border:0">
                        <div style="color:#E9D5FF;font-size:13px;letter-spacing:2px;font-weight:bold">ALERTAMUJER</div>
                        <h1 style="margin:8px 0 0;color:#FFFFFF;font-size:22px;line-height:1.3">%3$s</h1>
                      </td></tr>
                      <tr><td style="background:#FFFFFF;padding:26px 24px;font-size:15px;line-height:1.6;
                                     border-radius:0 0 18px 18px">%4$s</td></tr>
                      <tr><td style="padding:18px 12px;text-align:center;font-size:12px;line-height:1.5;color:%9$s">
                        %10$s<br>AlertaMujer · Tu red de apoyo cuando más la necesitas
                      </td></tr>
                    </table>
                  </td></tr>
                </table>
                </body></html>
                """.formatted(HtmlUtils.htmlEscape(resumen), HtmlUtils.htmlEscape(logoUrl), HtmlUtils.htmlEscape(titulo),
                body, VIOLETA_CLARO, VIOLETA_OSCURO, TEXTO, VIOLETA, TEXTO_SUAVE, pie);
    }

    /** Botón grande (un enlace con estilo de botón: los correos no tienen botones de verdad). */
    static String boton(String url, String text) {
        return """
                <p style="text-align:center;margin:26px 0">
                  <a href="%s" style="background:%s;color:#FFFFFF;padding:15px 28px;border-radius:30px;
                     text-decoration:none;font-weight:bold;font-size:16px;display:inline-block">%s</a>
                </p>""".formatted(HtmlUtils.htmlEscape(url), VIOLETA, HtmlUtils.htmlEscape(text));
    }

    /** Recuadro destacado (p. ej. el número de emergencias). */
    static String destacado(String html) {
        return "<p style=\"background:" + VIOLETA_CLARO + ";border-left:4px solid " + VIOLETA
                + ";border-radius:10px;padding:12px 14px;margin:18px 0\">" + html + "</p>";
    }

    static String nota(String html) {
        return "<p style=\"font-size:12px;color:" + TEXTO_SUAVE + ";margin:16px 0 0\">" + html + "</p>";
    }
}
