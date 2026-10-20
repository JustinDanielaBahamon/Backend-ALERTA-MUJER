package com.alertamujer.notifications.infrastructure.email;

import com.alertamujer.notifications.application.email.EmailSender;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Resend (API HTTP; Render gratis bloquea SMTP). Sin dominio verificado, Resend solo entrega
 * al correo dueño de la cuenta.
 */
public class ResendEmailSender implements EmailSender {

    private final RestClient client;
    private final String from;

    public ResendEmailSender(String apiKey, String from) {
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.from = from;
    }

    @Override
    public void send(EmailMessage m) {
        try {
            client.post().uri("/emails").contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("from", from, "to", List.of(m.to()), "subject", m.subject(), "html", m.html(), "text", m.text()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new EmailException("Resend respondió " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            throw new EmailException("No se pudo contactar a Resend: " + e.getMessage());
        }
    }

    @Override
    public String provider() {
        return "resend";
    }
}
