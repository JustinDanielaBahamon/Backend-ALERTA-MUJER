package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la alerta, sin Spring ni base de datos. */
class AlertTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static Alert activa() {
        Alert a = new Alert();
        a.setId(1L);
        a.setUserProfileId(10L);
        a.setStartedAt(AHORA.minusMinutes(5));
        return a;
    }

    private static CambiosAlerta estado(String estado) {
        return new CambiosAlerta(null, null, estado, null, null, null);
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

        assertTrue(a.actualizar(estado("cancelled"), AHORA));

        assertEquals("cancelled", a.getStatus());
        assertEquals(AHORA, a.getCancelledAt());
        assertNull(a.getEndedAt());
        assertTrue(a.terminada());
    }

    @Test
    void resolverRespetaLaHoraQueEnvioLaApp() {
        Alert a = activa();
        LocalDateTime enviada = AHORA.minusMinutes(1);

        assertTrue(a.actualizar(new CambiosAlerta(null, null, "resolved", null, enviada, null), AHORA));

        assertEquals(enviada, a.getEndedAt());
    }

    @Test
    void cambiosSinTerminarlaNoAvisanYSoloTocanLoEnviado() {
        Alert a = activa();
        a.setMessage("Ayuda");

        assertFalse(a.actualizar(new CambiosAlerta("silent", null, null, null, null, null), AHORA));

        assertEquals("silent", a.getAlertType());
        assertEquals("Ayuda", a.getMessage());
        assertTrue(a.estaActiva());
    }

    @Test
    void terminarUnaAlertaYaTerminadaNoVuelveAAvisar() {
        Alert a = activa();
        a.actualizar(estado("cancelled"), AHORA);

        assertFalse(a.actualizar(estado("resolved"), AHORA.plusMinutes(1)));
    }

    @Test
    void laAdministracionSoloAsignaEstadosConocidos() {
        Alert a = activa();

        assertThrows(IllegalArgumentException.class, () -> a.cambiarEstado("borrada", AHORA));
        assertThrows(IllegalArgumentException.class, () -> a.cambiarEstado(null, AHORA));
        assertTrue(a.cambiarEstado("resolved", AHORA));
        assertEquals(AHORA, a.getEndedAt());
        assertFalse(a.cambiarEstado("cancelled", AHORA));
        assertEquals(AHORA, a.getCancelledAt());
    }

    @Test
    void venceSoloLaActivaQueSuperaElLimite() {
        Alert vieja = activa();
        vieja.setStartedAt(AHORA.minusMinutes(31));
        Alert reciente = activa();

        assertTrue(vieja.vencida(AHORA.minusMinutes(30)));
        assertFalse(reciente.vencida(AHORA.minusMinutes(30)));

        vieja.expirar(AHORA);
        assertEquals("expired", vieja.getStatus());
        assertEquals(AHORA, vieja.getEndedAt());
        assertFalse(vieja.vencida(AHORA.minusMinutes(30)));
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
