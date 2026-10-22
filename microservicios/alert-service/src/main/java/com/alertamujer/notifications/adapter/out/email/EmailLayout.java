package com.alertamujer.notifications.adapter.out.email;

import org.springframework.web.util.HtmlUtils;

/**
 * Common email template: violet header with the app logo, white card and footer.
 * Built with tables and inline styles because Gmail and Outlook ignore external CSS and flexbox. The logo
 * is loaded from the public web (Gmail does not show images embedded as data URIs).
 */
final class EmailLayout {

    static final String VIOLET = "#7B1DB2";
    static final String DARK_VIOLET = "#2B0C3B";
    static final String LIGHT_VIOLET = "#F4EEFB";
    private static final String TEXT = "#2D1B3D";
    private static final String SOFT_TEXT = "#6E5A80";

    private EmailLayout() {}

    /** Public address of the logo (web/public/email/logo.png). */
    static String logoUrl(String webBaseUrl) {
        return webBaseUrl.replaceAll("/+$", "") + "/email/logo.png";
    }

    /**
     * @param summary short text Gmail shows next to the subject in the inbox (not visible in the email)
     * @param body    already escaped HTML
     * @param footer  already escaped HTML with the reason the email arrives
     */
    static String page(String logoUrl, String summary, String title, String body, String pie) {
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
                """.formatted(HtmlUtils.htmlEscape(summary), HtmlUtils.htmlEscape(logoUrl), HtmlUtils.htmlEscape(title),
                body, LIGHT_VIOLET, DARK_VIOLET, TEXT, VIOLET, SOFT_TEXT, pie);
    }

    /** Big button (a link styled as a button: emails do not have real buttons). */
    static String button(String url, String text) {
        return """
                <p style="text-align:center;margin:26px 0">
                  <a href="%s" style="background:%s;color:#FFFFFF;padding:15px 28px;border-radius:30px;
                     text-decoration:none;font-weight:bold;font-size:16px;display:inline-block">%s</a>
                </p>""".formatted(HtmlUtils.htmlEscape(url), VIOLET, HtmlUtils.htmlEscape(text));
    }

    /** Highlighted box (e.g. the emergency number). */
    static String highlighted(String html) {
        return "<p style=\"background:" + LIGHT_VIOLET + ";border-left:4px solid " + VIOLET
                + ";border-radius:10px;padding:12px 14px;margin:18px 0\">" + html + "</p>";
    }

    static String note(String html) {
        return "<p style=\"font-size:12px;color:" + SOFT_TEXT + ";margin:16px 0 0\">" + html + "</p>";
    }
}
