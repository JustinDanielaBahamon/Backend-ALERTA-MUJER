package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.GestionarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.NuevaAlerta;
import com.alertamujer.alerts.application.port.in.TrayectosUseCase.NuevoTrayecto;
import com.alertamujer.alerts.application.port.out.*;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.domain.model.SafeTrip;
import com.alertamujer.shared.exception.ConflictException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrayectosServiceTest {

    @Mock private TrayectoRepositorio trayectos;
    @Mock private IdentityPort identidad;
    @Mock private GestionarAlertasUseCase alerts;
    @Mock private UbicacionRepositorio ubicaciones;
    @Mock private CelularesPort celulares;
    @Mock private PushPort push;

    @InjectMocks private TrayectosService service;

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 10, 21, 20, 0);

    @BeforeEach
    void setUp() {
        lenient().when(identidad.perfilRequerido(1L)).thenReturn(10L);
        lenient().when(trayectos.save(any(SafeTrip.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static SafeTrip trayecto(int minutos) {
        SafeTrip t = SafeTrip.nuevo(10L, 1L, 2L, "Casa", minutos, null, null, SALIDA);
        t.setId(7L);
        return t;
    }

    @Test
    void iniciar_soloUnoALaVez() {
        when(trayectos.activoDe(10L)).thenReturn(Optional.of(trayecto(30)));

        assertThrows(ConflictException.class, () -> service.iniciar(new NuevoTrayecto("Casa", 30, 2L, null, null), 1L));
        verify(trayectos, never()).save(any());
    }

    @Test
    void iniciar_conLimiteDiario() {
        when(trayectos.activoDe(10L)).thenReturn(Optional.empty());
        when(trayectos.iniciadosDesde(eq(10L), any())).thenReturn((long) TrayectosService.MAXIMO_POR_DIA);

        assertThrows(IllegalArgumentException.class, () -> service.iniciar(new NuevoTrayecto("Casa", 30, 2L, null, null), 1L));
    }

    @Test
    void soloLaDuenaConfirmaOCancela() {
        when(trayectos.find(7L)).thenReturn(Optional.of(trayecto(30)));
        when(identidad.perfilRequerido(99L)).thenReturn(99L);

        assertThrows(ResourceNotFoundException.class, () -> service.llegue(7L, 99L));
        assertEquals(SafeTrip.LLEGO, service.llegue(7L, 1L).getStatus());
    }

    @Test
    void revisar_alertaConLaUltimaUbicacionCuandoNoConfirma() {
        SafeTrip t = trayecto(30);
        when(trayectos.activosQueLleganAntesDe(any())).thenReturn(List.of(t));
        LocationLog ultima = new LocationLog();
        ultima.setLatitude(new BigDecimal("2.93"));
        ultima.setLongitude(new BigDecimal("-75.28"));
        ultima.setRecordedAt(SALIDA.plusMinutes(25));
        when(ubicaciones.ultima(10L)).thenReturn(Optional.of(ultima));
        Alert creada = new Alert();
        creada.setId(55L);
        when(alerts.activar(any(NuevaAlerta.class), eq(1L))).thenReturn(creada);

        assertEquals(1, service.revisar(SALIDA.plusMinutes(33)));

        ArgumentCaptor<NuevaAlerta> nueva = ArgumentCaptor.forClass(NuevaAlerta.class);
        verify(alerts).activar(nueva.capture(), eq(1L));
        assertEquals("timer", nueva.getValue().activationMethod());
        assertEquals(new BigDecimal("2.93"), nueva.getValue().latitude());
        assertTrue(nueva.getValue().message().contains("Casa"));
        assertEquals(SafeTrip.ALERTADO, t.getStatus());
        assertEquals(55L, t.getAlertId());
    }

    @Test
    void revisar_preguntaSiLlegoAntesDeLaHora() {
        SafeTrip t = trayecto(30);
        when(trayectos.activosQueLleganAntesDe(any())).thenReturn(List.of(t));
        when(celulares.tokensDeCelulares(1L)).thenReturn(List.of("token"));

        assertEquals(0, service.revisar(SALIDA.plusMinutes(26)));

        verify(push).enviarDatos(eq(List.of("token")), argThat((Map<String, String> m) -> "TRAYECTO_POR_VENCER".equals(m.get("tipo"))));
        assertTrue(t.isReminderSent());
        verify(alerts, never()).activar(any(), any());
    }

    @Test
    void revisar_unErrorNoDetieneLosDemas() {
        SafeTrip malo = trayecto(30);
        SafeTrip bueno = trayecto(30);
        bueno.setId(8L);
        when(trayectos.activosQueLleganAntesDe(any())).thenReturn(List.of(malo, bueno));
        when(ubicaciones.ultima(10L)).thenReturn(Optional.empty());
        Alert creada = new Alert();
        creada.setId(56L);
        when(alerts.activar(any(), eq(1L))).thenThrow(new RuntimeException("caída")).thenReturn(creada);

        assertEquals(1, service.revisar(SALIDA.plusMinutes(40)));
        assertEquals(SafeTrip.ALERTADO, bueno.getStatus());
    }
}
