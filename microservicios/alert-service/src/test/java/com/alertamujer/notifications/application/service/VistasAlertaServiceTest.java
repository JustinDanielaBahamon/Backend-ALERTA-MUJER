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
    private final IdentityPort identidad = mock(IdentityPort.class);
    private final VistasRepositorio vistas = mock(VistasRepositorio.class);
    private final VistasAlertaService service = new VistasAlertaService(alerts, identidad, vistas);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 22, 10, 0);

    private void alertaDelPerfil(long perfil) {
        when(alerts.find(5L)).thenReturn(Optional.of(
                new AlertSummary(5L, perfil, "main", "panic_button", "active", null, null, null, null, null, null)));
    }

    @Test
    void laDuenaVeCuantosAbrieronYCuantosEstanMirando() {
        alertaDelPerfil(10L);
        when(identidad.profileOf(1L)).thenReturn(Optional.of(10L));
        when(vistas.contar(5L, AHORA.minusSeconds(90))).thenReturn(new Conteo(3, 1));

        assertEquals(new Conteo(3, 1), service.vistas(5L, 1L, AHORA));
    }

    @Test
    void otraPersonaNoPuedeVerlas() {
        alertaDelPerfil(10L);
        when(identidad.profileOf(2L)).thenReturn(Optional.of(20L));

        assertThrows(ResourceNotFoundException.class, () -> service.vistas(5L, 2L, AHORA));
        verifyNoInteractions(vistas);
    }
}
