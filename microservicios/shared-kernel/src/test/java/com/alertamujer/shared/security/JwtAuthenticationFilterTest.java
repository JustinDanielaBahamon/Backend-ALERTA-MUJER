package com.alertamujer.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filtro =
            new JwtAuthenticationFilter(new JwtKeyProvider("un-secreto-aleatorio-de-prueba-con-mas-de-32-caracteres"));

    private MockHttpServletResponse pedir(String ruta, MockFilterChain cadena) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", ruta);
        req.setRequestURI(ruta);
        req.addHeader("Authorization", "Bearer token-viejo-o-vencido");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(req, res, cadena);
        return res;
    }

    @Test
    void elSeguimientoPublicoIgnoraUnTokenViejo() throws Exception {
        MockFilterChain cadena = new MockFilterChain();
        MockHttpServletResponse res = pedir("/api/public/tracking/abc123", cadena);

        assertEquals(200, res.getStatus());
        assertNotNull(cadena.getRequest(), "la petición sigue hasta el controlador");
    }

    @Test
    void lasRutasPrivadasSiguenRechazandoUnTokenInvalido() throws Exception {
        MockFilterChain cadena = new MockFilterChain();
        MockHttpServletResponse res = pedir("/api/alerts/me", cadena);

        assertEquals(401, res.getStatus());
        assertNull(cadena.getRequest());
    }
}
