package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.PushPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class PedidoUbicacionServiceTest {

    private final CelularesPort celulares = mock(CelularesPort.class);
    private final PushPort push = mock(PushPort.class);
    private final PedidoUbicacionService service = new PedidoUbicacionService(celulares, push,
            Clock.fixed(Instant.parse("2026-10-16T15:00:00Z"), ZoneOffset.UTC));

    @Test
    void mandaUnPushSilencioso() {
        when(celulares.tokensDeCelulares(1L)).thenReturn(List.of("t1"));
        when(push.enviarDatos(anyList(), anyMap())).thenReturn(List.of());

        assertEquals(1, service.perform(1L).celulares());
        verify(push).enviarDatos(List.of("t1"), Map.of("tipo", "PEDIR_UBICACION"));
    }

    @Test
    void sinCelularConNotificacionesAvisa() {
        when(celulares.tokensDeCelulares(1L)).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> service.perform(1L));
        verifyNoInteractions(push);
    }

    @Test
    void siNoLlegaANingunCelularAvisa() {
        when(celulares.tokensDeCelulares(1L)).thenReturn(List.of("t1"));
        when(push.enviarDatos(anyList(), anyMap())).thenReturn(List.of("t1"));

        assertThrows(IllegalStateException.class, () -> service.perform(1L));
    }

    @Test
    void noRepiteElPedidoEnPocosSegundos() {
        when(celulares.tokensDeCelulares(1L)).thenReturn(List.of("t1"));
        when(push.enviarDatos(anyList(), anyMap())).thenReturn(List.of());

        service.perform(1L);
        assertThrows(IllegalStateException.class, () -> service.perform(1L));
    }
}
