package com.alertamujer.identity.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    private final RateLimitFilter filtro = new RateLimitFilter();

    private int post(String ruta, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ruta);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void bloqueaDespuesDelLimitePorIpYRuta() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_INTENTOS; i++) assertEquals(200, post("/api/auth/login", "1.1.1.1"));
        assertEquals(429, post("/api/auth/login", "1.1.1.1"));
        // Otra IP y otra ruta llevan su propio conteo
        assertEquals(200, post("/api/auth/login", "2.2.2.2"));
        assertEquals(200, post("/api/auth/google", "1.1.1.1"));
    }

    @Test
    void cubreRegistroYRecuperacion() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_INTENTOS; i++) post("/api/auth/register", "3.3.3.3");
        assertEquals(429, post("/api/auth/register", "3.3.3.3"));
        for (int i = 0; i < RateLimitFilter.MAX_INTENTOS; i++) post("/api/auth/forgot-password", "3.3.3.3");
        assertEquals(429, post("/api/auth/forgot-password", "3.3.3.3"));
    }

    @Test
    void noLimitaOtrasRutas() throws Exception {
        for (int i = 0; i < 10; i++) assertEquals(200, post("/api/alerts", "4.4.4.4"));
    }
}
