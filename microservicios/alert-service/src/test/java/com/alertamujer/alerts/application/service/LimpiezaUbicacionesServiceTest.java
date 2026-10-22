package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LimpiezaUbicacionesServiceTest {

    @Test
    void borraLoDelDiaADiaALos7DiasYLasAlertasALos30() {
        UbicacionRepositorio repository = mock(UbicacionRepositorio.class);
        LocalDateTime now = LocalDateTime.of(2026, 10, 19, 3, 30);
        when(repository.borrarSinAlertaAntesDe(now.minusDays(7))).thenReturn(120);
        when(repository.borrarDeAlertasAntesDe(now.minusDays(30))).thenReturn(15);

        assertEquals(135, new LimpiezaUbicacionesService(repository, 7, 30).clear(now));

        verify(repository).borrarSinAlertaAntesDe(now.minusDays(7));
        verify(repository).borrarDeAlertasAntesDe(now.minusDays(30));
    }
}
