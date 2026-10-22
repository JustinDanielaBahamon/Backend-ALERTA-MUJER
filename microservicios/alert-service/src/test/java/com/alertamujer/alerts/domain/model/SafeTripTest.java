package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SafeTripTest {

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 10, 21, 20, 0);

    private SafeTrip trayecto(int minutos) {
        return SafeTrip.nuevo(10L, 1L, 2L, "  Casa  ", minutos, null, null, SALIDA);
    }

    @Test
    void nuevo_calculaLaHoraDeLlegadaYValidaLosLimites() {
        SafeTrip t = trayecto(40);
        assertEquals("Casa", t.getDestination());
        assertEquals(SALIDA.plusMinutes(40), t.getExpectedArrival());
        assertTrue(t.activo());
        assertThrows(IllegalArgumentException.class, () -> trayecto(4));
        assertThrows(IllegalArgumentException.class, () -> trayecto(241));
        assertThrows(IllegalArgumentException.class, () -> SafeTrip.nuevo(10L, 1L, 2L, " ", 30, null, null, SALIDA));
    }

    @Test
    void preguntaCincoMinutosAntesYVenceConDosMinutosDeMargen() {
        SafeTrip t = trayecto(40);
        assertFalse(t.porVencer(SALIDA.plusMinutes(34)));
        assertTrue(t.porVencer(SALIDA.plusMinutes(35)));
        t.marcarRecordatorio();
        assertFalse(t.porVencer(SALIDA.plusMinutes(36)), "solo se pregunta una vez");
        assertFalse(t.vencido(SALIDA.plusMinutes(42)));
        assertTrue(t.vencido(SALIDA.plusMinutes(42).plusSeconds(1)));
    }

    @Test
    void alargarCorreLaHoraYVuelveAPreguntar() {
        SafeTrip t = trayecto(40);
        t.marcarRecordatorio();
        t.extender(15);
        assertEquals(SALIDA.plusMinutes(55), t.getExpectedArrival());
        assertTrue(t.porVencer(SALIDA.plusMinutes(50)));
        assertThrows(IllegalArgumentException.class, () -> t.extender(61));
    }

    @Test
    void noPuedeDurarMasDeSeisHoras() {
        SafeTrip t = trayecto(240);
        t.extender(60);
        t.extender(60);
        assertThrows(IllegalArgumentException.class, () -> t.extender(1));
    }

    @Test
    void unTrayectoTerminadoNoSeVuelveAUsar() {
        SafeTrip t = trayecto(30);
        t.llegar(SALIDA.plusMinutes(20));
        assertFalse(t.activo());
        assertFalse(t.vencido(SALIDA.plusHours(5)));
        assertThrows(IllegalArgumentException.class, () -> t.extender(10));
        assertThrows(IllegalArgumentException.class, () -> t.cancelar(SALIDA));
    }
}
