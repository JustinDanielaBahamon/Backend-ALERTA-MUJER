package com.alertamujer.settings.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la configuración global, sin Spring ni base de datos. */
class SystemConfigurationTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static SystemConfiguration config() {
        SystemConfiguration c = new SystemConfiguration();
        c.setSosMaxDurationMinutes(120);
        c.setEmergencyPhoneNumber("123");
        c.setAdminEmail("soporte@alertamujer.co");
        c.setMinAppVersion("1.0.0");
        return c;
    }

    @Test
    void cambiaSoloLoEnviadoYRegistraQuienYCuando() {
        SystemConfiguration c = config();

        c.actualizar(new CambiosAjustes(60, " +57123 ", null, 8, null), 7L, AHORA);

        assertEquals(60, c.getSosMaxDurationMinutes());
        assertEquals("+57123", c.getEmergencyPhoneNumber());
        assertEquals("soporte@alertamujer.co", c.getAdminEmail());
        assertEquals(8, c.getMaxEmergencyContacts());
        assertEquals("1.0.0", c.getMinAppVersion());
        assertEquals(7, c.getUpdatedByAdminId());
        assertEquals(AHORA, c.ultimaActualizacion());
    }

    @Test
    void unaVersionVaciaQuitaLaVersionMinima() {
        SystemConfiguration c = config();
        c.actualizar(new CambiosAjustes(null, null, null, null, " "), null, AHORA);
        assertNull(c.getMinAppVersion());
    }

    @Test
    void rechazaValoresFueraDeRango() {
        SystemConfiguration c = config();
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(29, null, null, null, null), 1L, AHORA));
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(1441, null, null, null, null), 1L, AHORA));
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(null, "12", null, null, null), 1L, AHORA));
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(null, null, "nada", null, null), 1L, AHORA));
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(null, null, null, 0, null), 1L, AHORA));
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(null, null, null, 21, null), 1L, AHORA));
        assertThrows(IllegalArgumentException.class, () -> c.actualizar(new CambiosAjustes(null, null, null, null, "v1"), 1L, AHORA));
        assertEquals(120, c.getSosMaxDurationMinutes());
    }

    @Test
    void sinEdicionesLaUltimaActualizacionEsLaCreacion() {
        SystemConfiguration c = config();
        assertEquals(c.getCreatedAt(), c.ultimaActualizacion());
    }
}
