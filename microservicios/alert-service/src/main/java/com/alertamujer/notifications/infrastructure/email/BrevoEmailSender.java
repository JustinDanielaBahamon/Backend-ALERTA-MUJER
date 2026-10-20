package com.alertamujer.notifications.infrastructure.email;

import com.alertamujer.notifications.application.email.EmailSender;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Brevo (API HTTP). Permite enviar desde un remitente verificado sin dominio propio. */
public class BrevoEmailSender implements EmailSender {

    private static final Pattern NOMBRE_Y_CORREO = Pattern.compile("^\\s*(.*?)\\s*<([^>]+)>\\s*$");

    private final RestClient client;
    private final Map<String, String> remitente;

    public BrevoEmailSender(String apiKey, String from) {
        this.client = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
        Matcher m = NOMBRE_Y_CORREO.matcher(from);
        this.remitente = m.matches() ? Map.of("name", m.group(1), "email", m.group(2)) : Map.of("email", from.trim());
    }

    @Override
    public void send(EmailMessage m) {
        try {
            client.post().uri("/smtp/email").contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("sender", remitente, "to", List.of(Map.of("email", m.to())), "subject", m.subject(),
                            "htmlContent", m.html(), "textContent", m.text()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new EmailException("Brevo respondió " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            throw new EmailException("No se pudo contactar a Brevo: " + e.getMessage());
        }
    }

    @Override
    public String provider() {
        return "brevo";
    }
}
