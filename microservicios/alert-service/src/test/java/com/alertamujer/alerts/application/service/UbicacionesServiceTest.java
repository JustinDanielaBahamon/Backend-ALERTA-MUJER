package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevaUbicacion;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UbicacionesServiceTest {

    @Mock private UbicacionRepositorio repository;
    @Mock private AlertaRepositorio alerts;
    @Mock private IdentityPort identidad;
    @Mock private CelularesPort celulares;

    @InjectMocks private UbicacionesService service;

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
        alert.setId(5L);
        alert.setUserProfileId(10L);
        lenient().when(identidad.perfilRequerido(1L)).thenReturn(10L);
        lenient().when(repository.save(any(LocationLog.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static NuevaUbicacion punto(Long alertId, Long deviceId) {
        return new NuevaUbicacion(BigDecimal.valueOf(4.6), BigDecimal.valueOf(-74.1), null, alertId, deviceId, null);
    }

    @Test
    void descartaLaUbicacionDiariaDeUnCelularQueNoEsElPrincipal() {
        when(celulares.esElMasReciente(1L, 8L)).thenReturn(false);

        assertTrue(service.save(punto(null, 8L), 1L).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void laUbicacionDeUnaAlertaSeGuardaVengaDelCelularQueVenga() {
        when(alerts.find(5L)).thenReturn(Optional.of(alert));

        LocationLog saved = service.save(punto(5L, 8L), 1L).orElseThrow();

        assertEquals(5L, saved.getAlertId());
        assertEquals(10L, saved.getUserProfileId());
        verifyNoInteractions(celulares);
    }

    @Test
    void unaAlertaAjenaNoSeAsociaAlPunto() {
        Alert ajena = new Alert();
        ajena.setId(6L);
        ajena.setUserProfileId(20L);
        when(alerts.find(6L)).thenReturn(Optional.of(ajena));
        when(celulares.esElMasReciente(1L, null)).thenReturn(true);

        assertNull(service.save(punto(6L, null), 1L).orElseThrow().getAlertId());
    }

    @Test
    void elRecorridoDeUnaAlertaSoloLoVenLaDuenaYLaAdministracion() {
        when(alerts.find(5L)).thenReturn(Optional.of(alert));
        when(identidad.perfilRequerido(2L)).thenReturn(20L);
        when(repository.ofAlert(5L)).thenReturn(List.of());

        assertThrows(AccessDeniedException.class, () -> service.ofAlert(5L, 2L, false));
        assertEquals(List.of(), service.ofAlert(5L, 2L, true));
        assertEquals(List.of(), service.ofAlert(5L, 1L, false));
    }

    @Test
    void laAdministracionNoVeElRecorridoDeAlertasCanceladasOVencidas() {
        when(alerts.find(5L)).thenReturn(Optional.of(alert));
        when(identidad.perfilRequerido(2L)).thenReturn(20L);
        when(repository.ofAlert(5L)).thenReturn(List.of());

        alert.setStatus("resolved");
        assertEquals(List.of(), service.ofAlert(5L, 2L, true));
        alert.setStatus("cancelled");
        assertThrows(AccessDeniedException.class, () -> service.ofAlert(5L, 2L, true));
        alert.setStatus("expired");
        assertThrows(AccessDeniedException.class, () -> service.ofAlert(5L, 2L, true));
        // La dueña sí la ve
        assertEquals(List.of(), service.ofAlert(5L, 1L, false));
    }

    @Test
    void recientesLimitaElNumeroDePuntos() {
        service.recientes(1L, 10_000);
        service.recientes(1L, 0);

        verify(repository).recientes(10L, 500);
        verify(repository).recientes(10L, 1);
    }
}
