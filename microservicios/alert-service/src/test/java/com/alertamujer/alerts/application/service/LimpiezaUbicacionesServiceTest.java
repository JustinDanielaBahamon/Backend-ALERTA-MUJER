package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LimpiezaUbicacionesServiceTest {

    @Test
    void borraLoDelDiaADiaALos7DiasYLasAlertasALos30() {
        UbicacionRepositorio repositorio = mock(UbicacionRepositorio.class);
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 19, 3, 30);
        when(repositorio.borrarSinAlertaAntesDe(ahora.minusDays(7))).thenReturn(120);
        when(repositorio.borrarDeAlertasAntesDe(ahora.minusDays(30))).thenReturn(15);

        assertEquals(135, new LimpiezaUbicacionesService(repositorio, 7, 30).limpiar(ahora));

        verify(repositorio).borrarSinAlertaAntesDe(ahora.minusDays(7));
        verify(repositorio).borrarDeAlertasAntesDe(ahora.minusDays(30));
    }
}
