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
public class EventosRemotos {

    private static final Logger log = LoggerFactory.getLogger(EventosRemotos.class);

    private final RestClient http;
    private final ObjectMapper json;

    public EventosRemotos(String urlServicio, String clave, ObjectMapper json) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(90_000);
        tiempos.setReadTimeout(90_000);
        this.http = RestClient.builder()
                .baseUrl(urlServicio.replaceAll("/+$", "") + "/internal/events")
                .requestFactory(tiempos)
                .defaultHeader(ClaveInterna.CABECERA, clave)
                .build();
        this.json = json;
    }

    public void enviar(Object evento) {
        try {
            http.post().uri("/" + evento.getClass().getSimpleName())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.writeValueAsString(evento))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo enviar el evento {} a otro microservicio: {}", evento.getClass().getSimpleName(), e.getMessage());
        }
    }
}
