package com.alertamujer.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void rutaInexistenteDevuelve404() {
        var r = handler.handleSpringErrorResponse(new NoResourceFoundException(HttpMethod.GET, "api/nada"));
        assertEquals(404, r.getStatusCode().value());
    }

    @Test
    void metodoNoPermitidoDevuelve405() {
        var r = handler.handleSpringErrorResponse(new HttpRequestMethodNotSupportedException("PATCH"));
        assertEquals(405, r.getStatusCode().value());
    }

    @Test
    void errorInternoNoExponeElMensaje() {
        var r = handler.handleRuntime(new RuntimeException("SELECT * FROM identity.user_account falló"));
        assertEquals(500, r.getStatusCode().value());
        assertFalse(r.getBody().getMessage().contains("SELECT"));
    }
}
