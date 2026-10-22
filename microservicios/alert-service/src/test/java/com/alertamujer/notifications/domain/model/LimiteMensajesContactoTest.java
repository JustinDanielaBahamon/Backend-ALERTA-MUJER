package com.alertamujer.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LimiteMensajesContactoTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void unMensajeCadaDosSegundosPorEnlace() {
        LimiteMensajesContacto limite = new LimiteMensajesContacto();

        limite.registrar("enlace", NOW);
        assertThrows(IllegalArgumentException.class, () -> limite.registrar("enlace", NOW.plusSeconds(1)));
        assertDoesNotThrow(() -> limite.registrar("otro", NOW.plusSeconds(1)));
        assertDoesNotThrow(() -> limite.registrar("enlace", NOW.plusSeconds(3)));
    }

    @Test
    void comoMucho30CadaDiezMinutos() {
        LimiteMensajesContacto limite = new LimiteMensajesContacto();
        for (int i = 0; i < LimiteMensajesContacto.MAXIMO_EN_VENTANA; i++) limite.registrar("enlace", NOW.plusSeconds(3L * i));

        assertThrows(IllegalArgumentException.class, () -> limite.registrar("enlace", NOW.plusSeconds(100)));
        assertDoesNotThrow(() -> limite.registrar("enlace", NOW.plusMinutes(11)));
    }
}
