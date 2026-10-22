package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.AlertasRecibidasUseCase.ReceivedAlert;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AlertasRecibidasServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final SeguimientoPublicoUseCase seguimiento = mock(SeguimientoPublicoUseCase.class);
    private final AlertasRecibidasService service = new AlertasRecibidasService(alerts, identity, seguimiento);

    private static AlertSummary alert(Long id, Long profile, String status, LocalDateTime start, LocalDateTime fin) {
        return new AlertSummary(id, profile, "main", "panic_button", status, null, start, null, null, fin, null);
    }

    @BeforeEach
    void setUp() {
        when(identity.correoDe(1L)).thenReturn(Optional.of("bea@correo.com"));
        when(identity.profileOf(1L)).thenReturn(Optional.of(11L));
        when(identity.nombreDePilaDelPerfil(10L)).thenReturn(Optional.of("Ana"));
        when(alerts.avisadasA("bea@correo.com")).thenReturn(List.of(1L, 2L, 3L, 4L, 5L));
        when(alerts.findMany(List.of(1L, 2L, 3L, 4L, 5L))).thenReturn(List.of(
                alert(1L, 10L, "resolved", NOW.minusDays(2), NOW.minusDays(2).plusHours(1)),
                alert(2L, 10L, "active", NOW.minusMinutes(5), null),
                alert(3L, 11L, "active", NOW.minusMinutes(1), null),      // propia
                alert(4L, 10L, "resolved", NOW.minusDays(40), NOW.minusDays(40)),  // vieja
                alert(5L, 10L, "cancelled", NOW.minusHours(1), NOW.minusMinutes(30))));
    }

    @Test
    void listaSoloLasDeOtrasPersonasYRecientesDeLaMasNueva() {
        List<ReceivedAlert> r = service.recibidas(1L, NOW);

        assertEquals(List.of(2L, 5L, 1L), r.stream().map(ReceivedAlert::alertId).toList());
        assertEquals("Ana", r.get(0).firstName());
        assertTrue(r.get(0).trackingAvailable());
        assertTrue(r.get(1).trackingAvailable());
        assertFalse(r.get(2).trackingAvailable());
    }

    @Test
    void creaElEnlaceSoloDeUnaAlertaRecibidaYVigente() {
        when(seguimiento.crearEnlace(2L)).thenReturn("CODIGO");

        assertEquals("CODIGO", service.enlaceSeguimiento(1L, 2L, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.enlaceSeguimiento(1L, 1L, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.enlaceSeguimiento(1L, 3L, NOW));
        verify(seguimiento, times(1)).crearEnlace(anyLong());
    }
}
