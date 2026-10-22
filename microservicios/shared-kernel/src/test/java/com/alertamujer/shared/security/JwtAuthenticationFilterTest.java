package com.alertamujer.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(new JwtKeyProvider("un-secreto-aleatorio-de-prueba-con-mas-de-32-caracteres"));

    private MockHttpServletResponse perform(String path, MockFilterChain chain) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", path);
        req.setRequestURI(path);
        req.addHeader("Authorization", "Bearer token-viejo-o-vencido");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        return res;
    }

    @Test
    void publicTrackingIgnoresAnOldToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse res = perform("/api/public/tracking/abc123", chain);

        assertEquals(200, res.getStatus());
        assertNotNull(chain.getRequest(), "la petición sigue hasta el controlador");
    }

    @Test
    void privateRoutesStillRejectAnInvalidToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse res = perform("/api/alerts/me", chain);

        assertEquals(401, res.getStatus());
        assertNull(chain.getRequest());
    }
}
