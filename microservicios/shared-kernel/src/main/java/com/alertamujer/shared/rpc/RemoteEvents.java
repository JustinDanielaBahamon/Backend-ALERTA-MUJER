package com.alertamujer.shared.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Sends a domain event to another microservice (POST /internal/events/{Event}). Replaces the
 * in-memory monolith event when the listener lives in another service. No message broker:
 * if the other service does not answer, the send is logged and does not block the sender.
 */
public class RemoteEvents {

    private static final Logger log = LoggerFactory.getLogger(RemoteEvents.class);

    private final RestClient http;
    private final ObjectMapper json;

    public RemoteEvents(String serviceUrl, String key, ObjectMapper json) {
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(90_000);
        timeouts.setReadTimeout(90_000);
        this.http = RestClient.builder()
                .baseUrl(serviceUrl.replaceAll("/+$", "") + "/internal/events")
                .requestFactory(timeouts)
                .defaultHeader(InternalKey.HEADER, key)
                .build();
        this.json = json;
    }

    public void send(Object event) {
        try {
            http.post().uri("/" + event.getClass().getSimpleName())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.writeValueAsString(event))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo enviar el evento {} a otro microservicio: {}", event.getClass().getSimpleName(), e.getMessage());
        }
    }
}
