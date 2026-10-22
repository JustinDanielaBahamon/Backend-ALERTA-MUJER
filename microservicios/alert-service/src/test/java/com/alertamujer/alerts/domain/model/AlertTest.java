package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la alerta, sin Spring ni base de datos. */
class AlertTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static Alert activa() {
        Alert a = new Alert();
        a.setId(1L);
        a.setUserProfileId(10L);
        a.setStartedAt(NOW.minusMinutes(5));
        return a;
    }

    private static CambiosAlerta status(String status) {
        return new CambiosAlerta(null, null, status, null, null, null);
    }

    @Test
    void unaAlertaNuevaEstaActivaYEsPrincipal() {
        Alert a = new Alert();
        assertTrue(a.estaActiva());
        assertEquals("main", a.getAlertType());
        assertFalse(a.isKeepEvidence());
        assertFalse(a.terminada());
    }

    @Test
    void esDeSoloSuDuena() {
        Alert a = activa();
        assertTrue(a.esDe(10L));
        assertFalse(a.esDe(20L));
        assertFalse(a.esDe(null));
    }

    @Test
    void cancelarUnaAlertaActivaGuardaLaHoraYAvisaQueTermino() {
        Alert a = activa();

        assertTrue(a.update(status("cancelled"), NOW));

        assertEquals("cancelled", a.getStatus());
        assertEquals(NOW, a.getCancelledAt());
        assertNull(a.getEndedAt());
        assertTrue(a.terminada());
    }

    @Test
    void resolverRespetaLaHoraQueEnvioLaApp() {
        Alert a = activa();
        LocalDateTime enviada = NOW.minusMinutes(1);

        assertTrue(a.update(new CambiosAlerta(null, null, "resolved", null, enviada, null), NOW));

        assertEquals(enviada, a.getEndedAt());
    }

    @Test
    void cambiosSinTerminarlaNoAvisanYSoloTocanLoEnviado() {
        Alert a = activa();
        a.setMessage("Ayuda");

        assertFalse(a.update(new CambiosAlerta("silent", null, null, null, null, null), NOW));

        assertEquals("silent", a.getAlertType());
        assertEquals("Ayuda", a.getMessage());
        assertTrue(a.estaActiva());
    }

    @Test
    void terminarUnaAlertaYaTerminadaNoVuelveAAvisar() {
        Alert a = activa();
        a.update(status("cancelled"), NOW);

        assertFalse(a.update(status("resolved"), NOW.plusMinutes(1)));
    }

    @Test
    void theAdministrationOnlyAssignsKnownStatuses() {
        Alert a = activa();

        assertThrows(IllegalArgumentException.class, () -> a.changeStatus("borrada", NOW));
        assertThrows(IllegalArgumentException.class, () -> a.changeStatus(null, NOW));
        assertTrue(a.changeStatus("resolved", NOW));
        assertEquals(NOW, a.getEndedAt());
        assertFalse(a.changeStatus("cancelled", NOW));
        assertEquals(NOW, a.getCancelledAt());
    }

    @Test
    void venceSoloLaActivaQueSuperaElLimite() {
        Alert vieja = activa();
        vieja.setStartedAt(NOW.minusMinutes(31));
        Alert recent = activa();

        assertTrue(vieja.expired(NOW.minusMinutes(30)));
        assertFalse(recent.expired(NOW.minusMinutes(30)));

        vieja.expire(NOW);
        assertEquals("expired", vieja.getStatus());
        assertEquals(NOW, vieja.getEndedAt());
        assertFalse(vieja.expired(NOW.minusMinutes(30)));
    }

    @Test
    void conservaEvidenciasDeHasta5Alertas() {
        Alert a = activa();

        a.conservarEvidencias(true, 4);
        assertTrue(a.isKeepEvidence());

        // Ya conservada: volver a marcarla no cuenta como una más
        a.conservarEvidencias(true, Alert.MAX_ALERTAS_CONSERVADAS);
        a.conservarEvidencias(false, Alert.MAX_ALERTAS_CONSERVADAS);
        assertFalse(a.isKeepEvidence());

        assertThrows(IllegalArgumentException.class, () -> a.conservarEvidencias(true, Alert.MAX_ALERTAS_CONSERVADAS));
        assertFalse(a.isKeepEvidence());
    }
}
