package com.alertamujer.shared.exception;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unknownPathReturns404() {
        var r = handler.handleSpringErrorResponse(new NoResourceFoundException(HttpMethod.GET, "api/nada"));
        assertEquals(404, r.getStatusCode().value());
    }

    @Test
    void methodNotAllowedReturns405() {
        var r = handler.handleSpringErrorResponse(new HttpRequestMethodNotSupportedException("PATCH"));
        assertEquals(405, r.getStatusCode().value());
    }

    @Test
    void missingHeaderReturns400() throws NoSuchMethodException {
        var parameter = new MethodParameter(getClass().getDeclaredMethod("withKey", String.class), 0);
        var r = handler.handleSpringErrorResponse(new MissingRequestHeaderException("X-Protection-Key", parameter));
        assertEquals(400, r.getStatusCode().value());
    }

    @SuppressWarnings("unused")
    private void withKey(String key) {}

    @Test
    void internalErrorDoesNotExposeTheMessage() {
        var r = handler.handleRuntime(new RuntimeException("SELECT * FROM identity.user_account falló"));
        assertEquals(500, r.getStatusCode().value());
        assertFalse(r.getBody().getMessage().contains("SELECT"));
    }
}
