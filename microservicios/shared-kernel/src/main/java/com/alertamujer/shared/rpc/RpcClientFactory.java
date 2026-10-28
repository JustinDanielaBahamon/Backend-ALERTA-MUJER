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
 * Generic HTTP client between microservices (Feign style): creates an implementation of a
 * *Api contract that, instead of running local code, calls POST /internal/rpc/{Contract}/{method}
 * on the owning service. So code using IdentityApi, AlertsApi... does not change when services are
 * split: only the injected adapter changes.
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
    public <T> T create(Class<T> contract, String serviceUrl) {
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        // Render wakes a sleeping service in ~1 minute: wait up to 90 s the first time
        timeouts.setConnectTimeout(90_000);
        timeouts.setReadTimeout(90_000);
        RestClient http = RestClient.builder()
                .baseUrl(serviceUrl.replaceAll("/+$", "") + "/internal/rpc/" + contract.getSimpleName())
                .requestFactory(timeouts)
                .defaultHeader(InternalKey.HEADER, key)
                .build();

        return (T) Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "Cliente HTTP de " + contract.getSimpleName() + " en " + serviceUrl;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            }
            Object[] arguments = args == null ? new Object[0] : args;
            try {
                String response = http.post().uri("/" + method.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(json.writeValueAsString(Arrays.asList(arguments)))
                        .retrieve()
                        .body(String.class);
                if (method.getReturnType() == void.class) return null;
                JavaType type = json.getTypeFactory().constructType(method.getGenericReturnType());
                if (response == null || response.isBlank()) return json.readValue("null", type);
                return json.readValue(response, type);
            } catch (RestClientResponseException e) {
                throw translate(e);
            }
        });
    }

    /** Rethrows on the client side the same kind of error that happened in the remote service. */
    private RuntimeException translate(RestClientResponseException e) {
        String message = e.getStatusText();
        try {
            RemoteError error = json.readValue(e.getResponseBodyAsString(), RemoteError.class);
            if (error.message() != null) message = error.message();
        } catch (Exception ignored) {
            // Response without a JSON body (service down, Render proxy...)
        }
        return switch (e.getStatusCode().value()) {
            case 404 -> new ResourceNotFoundException(message);
            case 403 -> new AccessDeniedException(message);
            case 400 -> new IllegalArgumentException(message);
            case 409 -> new IllegalStateException(message);
            default -> new IllegalStateException("Error en otro microservicio: " + message);
        };
    }
}
