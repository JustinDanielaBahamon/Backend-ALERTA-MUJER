package com.alertamujer.microservicio;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.shared.rpc.ClaveInterna;
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
    private final String clave;

    EvidenceApiHttpClient(@Value("${services.evidence.url}") String url, @Value("${internal.api-key:}") String clave) {
        this.base = url.replaceAll("/+$", "") + "/internal/evidence";
        this.clave = clave;
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(90_000);
        tiempos.setReadTimeout(90_000);
        this.http = RestClient.builder().baseUrl(base).requestFactory(tiempos)
                .defaultHeader(ClaveInterna.CABECERA, clave).build();
    }

    @Override
    public List<EvidenceSummary> evidenciasDeAlerta(Long alertId) {
        List<EvidenceSummary> lista = http.get().uri("/alerts/{a}", alertId).retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return lista == null ? List.of() : lista;
    }

    @Override
    public Optional<EvidenceFile> archivo(Long alertId, Long evidenceId) {
        try {
            DatosArchivo d = http.get().uri("/alerts/{a}/evidences/{e}", alertId, evidenceId).retrieve().body(DatosArchivo.class);
            if (d == null) return Optional.empty();
            String contenido = base + "/alerts/" + alertId + "/evidences/" + evidenceId + "/content";
            return Optional.of(new EvidenceFile(d.contentType(), d.fileName(), new ContenidoRemoto(contenido, clave),
                    Optional.ofNullable(d.enlaceTemporal()).map(URI::create)));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    /** Datos de un archivo de evidencia (sin el contenido). */
    record DatosArchivo(String contentType, String fileName, String enlaceTemporal) {}

    /** Contenido del archivo en Evidence Service; se descarga al leerlo, con la clave interna. */
    static final class ContenidoRemoto extends AbstractResource {
        private final String url;
        private final String clave;

        ContenidoRemoto(String url, String clave) {
            this.url = url;
            this.clave = clave;
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
            conexion.setRequestProperty(ClaveInterna.CABECERA, clave);
            conexion.setConnectTimeout(90_000);
            conexion.setReadTimeout(90_000);
            if (conexion.getResponseCode() != 200) throw new IOException("Evidence Service respondió " + conexion.getResponseCode());
            return conexion.getInputStream();
        }
    }
}
