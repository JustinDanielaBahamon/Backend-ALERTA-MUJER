package com.alertamujer.shared.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Envía un evento de dominio a otro microservicio (POST /internal/events/{Evento}). Reemplaza al
 * evento en memoria del monolito cuando quien lo escucha vive en otro servicio. Sin broker de
 * mensajería: si el otro servicio no responde, el envío se registra en el log y no frena al emisor.
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
