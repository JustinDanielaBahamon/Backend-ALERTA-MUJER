package com.alertamujer.shared.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestInfoTest {

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void conUserAgent(String ua) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (ua != null) request.addHeader("User-Agent", ua);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void source_distinguishesMobileAppAndWebPanel() {
        conUserAgent("okhttp/4.12.0");
        assertEquals("App móvil · Android", RequestInfo.source());

        conUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/130.0 Safari/537.36");
        assertEquals("Panel web · Chrome · Linux", RequestInfo.source());

        conUserAgent("curl/8.5.0");
        assertEquals("Cliente curl", RequestInfo.source());
    }

    @Test
    void source_withoutRequestIsNull() {
        assertNull(RequestInfo.source());
    }
}
