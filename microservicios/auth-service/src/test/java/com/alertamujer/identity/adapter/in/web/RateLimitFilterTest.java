package com.alertamujer.identity.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    private int post(String path, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void blocksAfterTheLimitPerIpAndPath() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_ATTEMPTS; i++) assertEquals(200, post("/api/auth/login", "1.1.1.1"));
        assertEquals(429, post("/api/auth/login", "1.1.1.1"));
        // Otra IP y otra ruta llevan su propio conteo
        assertEquals(200, post("/api/auth/login", "2.2.2.2"));
        assertEquals(200, post("/api/auth/google", "1.1.1.1"));
    }

    @Test
    void coversRegistrationAndRecovery() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_ATTEMPTS; i++) post("/api/auth/register", "3.3.3.3");
        assertEquals(429, post("/api/auth/register", "3.3.3.3"));
        for (int i = 0; i < RateLimitFilter.MAX_ATTEMPTS; i++) post("/api/auth/forgot-password", "3.3.3.3");
        assertEquals(429, post("/api/auth/forgot-password", "3.3.3.3"));
    }

    @Test
    void doesNotLimitOtherPaths() throws Exception {
        for (int i = 0; i < 10; i++) assertEquals(200, post("/api/alerts", "4.4.4.4"));
    }
}
