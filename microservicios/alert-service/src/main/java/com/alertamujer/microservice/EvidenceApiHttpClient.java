package com.alertamujer.microservice;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.shared.rpc.InternalKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.AbstractResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida hacia Evidence Service para la página de seguimiento de los contactos.
 * Los archivos no viajan en JSON: se piden sus datos y su enlace firmado de B2; el contenido se
 * descarga aparte, solo si hace falta (almacenamiento en disco, sin enlaces firmados).
 */
@Component
class EvidenceApiHttpClient implements EvidenceApi {

    private final RestClient http;
    private final String base;
    private final String key;

    EvidenceApiHttpClient(@Value("${services.evidence.url}") String url, @Value("${internal.api-key:}") String key) {
        this.base = url.replaceAll("/+$", "") + "/internal/evidence";
        this.key = key;
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(90_000);
        timeouts.setReadTimeout(90_000);
        this.http = RestClient.builder().baseUrl(base).requestFactory(timeouts)
                .defaultHeader(InternalKey.HEADER, key).build();
    }

    @Override
    public List<EvidenceSummary> alertEvidence(Long alertId) {
        List<EvidenceSummary> list = http.get().uri("/alerts/{a}", alertId).retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return list == null ? List.of() : list;
    }

    @Override
    public Optional<EvidenceFile> file(Long alertId, Long evidenceId) {
        try {
            FileData d = http.get().uri("/alerts/{a}/evidences/{e}", alertId, evidenceId).retrieve().body(FileData.class);
            if (d == null) return Optional.empty();
            String content = base + "/alerts/" + alertId + "/evidences/" + evidenceId + "/content";
            return Optional.of(new EvidenceFile(d.contentType(), d.fileName(), new ContenidoRemoto(content, key),
                    Optional.ofNullable(d.temporaryLink()).map(URI::create)));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    /** Datos de un archivo de evidencia (sin el contenido). */
    record FileData(String contentType, String fileName, String temporaryLink) {}

    /** Contenido del archivo en Evidence Service; se descarga al leerlo, con la clave interna. */
    static final class ContenidoRemoto extends AbstractResource {
        private final String url;
        private final String key;

        ContenidoRemoto(String url, String key) {
            this.url = url;
            this.key = key;
        }

        @Override
        public String getDescription() {
            return "Evidencia en " + url;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            HttpURLConnection conexion = (HttpURLConnection) new URL(url).openConnection();
            conexion.setRequestProperty(InternalKey.HEADER, key);
            conexion.setConnectTimeout(90_000);
            conexion.setReadTimeout(90_000);
            if (conexion.getResponseCode() != 200) throw new IOException("Evidence Service respondió " + conexion.getResponseCode());
            return conexion.getInputStream();
        }
    }
}
