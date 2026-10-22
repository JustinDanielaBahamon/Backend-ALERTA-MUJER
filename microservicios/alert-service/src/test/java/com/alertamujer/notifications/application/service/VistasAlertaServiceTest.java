package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.VistasRepositorio;
import com.alertamujer.notifications.application.port.out.VistasRepositorio.Conteo;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VistasAlertaServiceTest {

    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final VistasRepositorio vistas = mock(VistasRepositorio.class);
    private final VistasAlertaService service = new VistasAlertaService(alerts, identity, vistas);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 10, 0);

    private void alertaDelPerfil(long profile) {
        when(alerts.find(5L)).thenReturn(Optional.of(
                new AlertSummary(5L, profile, "main", "panic_button", "active", null, null, null, null, null, null)));
    }

    @Test
    void laDuenaVeCuantosAbrieronYCuantosEstanMirando() {
        alertaDelPerfil(10L);
        when(identity.profileOf(1L)).thenReturn(Optional.of(10L));
        when(vistas.count(5L, NOW.minusSeconds(90))).thenReturn(new Conteo(3, 1));

        assertEquals(new Conteo(3, 1), service.vistas(5L, 1L, NOW));
    }

    @Test
    void otraPersonaNoPuedeVerlas() {
        alertaDelPerfil(10L);
        when(identity.profileOf(2L)).thenReturn(Optional.of(20L));

        assertThrows(ResourceNotFoundException.class, () -> service.vistas(5L, 2L, NOW));
        verifyNoInteractions(vistas);
    }
}
