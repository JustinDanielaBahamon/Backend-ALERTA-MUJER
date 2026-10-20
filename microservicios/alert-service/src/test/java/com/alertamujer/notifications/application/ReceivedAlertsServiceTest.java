package com.alertamujer.notifications.application;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceivedAlertsServiceTest {

    @Mock private AlertsApi alertsApi;
    @Mock private IdentityApi identityApi;
    @Mock private TrackingLinkService trackingLinkService;
    @InjectMocks private ReceivedAlertsService service;

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 12, 0);

    private AlertSummary alerta(Long id, Long perfil, String estado, LocalDateTime inicio, LocalDateTime fin) {
        return new AlertSummary(id, perfil, "main", "panic_button", estado, null, inicio, null, null, fin, null);
    }

    @BeforeEach
    void setUp() {
        when(identityApi.findUser(1L)).thenReturn(Optional.of(new UserSummary(1L, "Maria", "R", "maria@correo.com", null, 1L, null)));
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(10L));
        when(alertsApi.alertsNotifiedTo("maria@correo.com")).thenReturn(List.of(5L, 6L, 7L, 8L));
        when(alertsApi.findAlerts(anyCollection())).thenReturn(Map.of(
                5L, alerta(5L, 20L, "active", AHORA.minusMinutes(10), null),
                6L, alerta(6L, 21L, "resolved", AHORA.minusHours(5), AHORA.minusHours(4)),
                7L, alerta(7L, 10L, "active", AHORA.minusMinutes(1), null),      // propia
                8L, alerta(8L, 22L, "resolved", AHORA.minusDays(40), AHORA.minusDays(40))));
        lenient().when(identityApi.findUserByProfileId(anyLong()))
                .thenReturn(Optional.of(new UserSummary(2L, "Ana", "G", "ana@correo.com", null, 1L, null)));
    }

    @Test
    void listaSoloLasDeOtrasPersonasYRecientes() {
        var recibidas = service.recibidas(1L, AHORA);

        assertEquals(List.of(5L, 6L), recibidas.stream().map(ReceivedAlertsService.ReceivedAlert::alertId).toList());
        assertTrue(recibidas.get(0).active());
        assertTrue(recibidas.get(0).trackingAvailable());
        assertFalse(recibidas.get(1).trackingAvailable()); // terminó hace 4 h
        assertEquals("Ana", recibidas.get(0).firstName());
    }

    @Test
    void creaElEnlaceDeUnaAlertaRecibidaYVigente() {
        when(trackingLinkService.crear(5L)).thenReturn("codigo-nuevo");
        assertEquals("codigo-nuevo", service.enlaceSeguimiento(1L, 5L, AHORA));
    }

    @Test
    void noCreaEnlacesDeAlertasAjenasNiTerminadas() {
        assertThrows(ResourceNotFoundException.class, () -> service.enlaceSeguimiento(1L, 99L, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.enlaceSeguimiento(1L, 6L, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.enlaceSeguimiento(1L, 7L, AHORA));
        verify(trackingLinkService, never()).crear(anyLong());
    }
}
