package com.alertamujer.shared.rpc;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Cliente HTTP genérico entre microservicios (al estilo de Feign): crea una implementación de un
 * contrato *Api que, en vez de ejecutar el código local, llama a POST /internal/rpc/{Contrato}/{metodo}
 * del servicio dueño. Así el código que usa IdentityApi, AlertsApi... no cambia al separar servicios:
 * solo cambia el adaptador que se inyecta.
 */
@Component
public class RpcClientFactory {

    private final ObjectMapper json;
    private final String key;

    public RpcClientFactory(ObjectMapper json, @Value("${internal.api-key:}") String key) {
        this.json = json;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> contrato, String urlServicio) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        // Render despierta un servicio dormido en ~1 minuto: se espera hasta 90 s la primera vez
        tiempos.setConnectTimeout(90_000);
        tiempos.setReadTimeout(90_000);
        RestClient http = RestClient.builder()
                .baseUrl(urlServicio.replaceAll("/+$", "") + "/internal/rpc/" + contrato.getSimpleName())
                .requestFactory(tiempos)
                .defaultHeader(ClaveInterna.CABECERA, key)
                .build();

        return (T) Proxy.newProxyInstance(contrato.getClassLoader(), new Class<?>[]{contrato}, (proxy, metodo, args) -> {
            if (metodo.getDeclaringClass() == Object.class) {
                return switch (metodo.getName()) {
                    case "toString" -> "Cliente HTTP de " + contrato.getSimpleName() + " en " + urlServicio;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(metodo.getName());
                };
            }
            Object[] argumentos = args == null ? new Object[0] : args;
            try {
                String respuesta = http.post().uri("/" + metodo.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(json.writeValueAsString(Arrays.asList(argumentos)))
                        .retrieve()
                        .body(String.class);
                if (metodo.getReturnType() == void.class) return null;
                JavaType type = json.getTypeFactory().constructType(metodo.getGenericReturnType());
                if (respuesta == null || respuesta.isBlank()) return json.readValue("null", type);
                return json.readValue(respuesta, type);
            } catch (RestClientResponseException e) {
                throw traducir(e);
            }
        });
    }

    /** Vuelve a lanzar del lado cliente el mismo tipo de error que ocurrió en el servicio remoto. */
    private RuntimeException traducir(RestClientResponseException e) {
        String mensaje = e.getStatusText();
        try {
            ErrorRemoto error = json.readValue(e.getResponseBodyAsString(), ErrorRemoto.class);
            if (error.mensaje() != null) mensaje = error.mensaje();
        } catch (Exception ignorado) {
            // Respuesta sin cuerpo JSON (servicio caído, proxy de Render...)
        }
        return switch (e.getStatusCode().value()) {
            case 404 -> new ResourceNotFoundException(mensaje);
            case 403 -> new AccessDeniedException(mensaje);
            case 400 -> new IllegalArgumentException(mensaje);
            case 409 -> new IllegalStateException(mensaje);
            default -> new IllegalStateException("Error en otro microservicio: " + mensaje);
        };
    }
}
