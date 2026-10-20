package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase.TrackingView;
import com.alertamujer.notifications.application.port.out.*;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SeguimientoPublicoServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);
    private static final String CODIGO = TrackingLink.generarCodigo();

    private final EnlaceRepositorio enlaces = mock(EnlaceRepositorio.class);
    private final AlertasPort alertas = mock(AlertasPort.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final AjustesPort ajustes = mock(AjustesPort.class);
    private final EvidenciasPort evidencias = mock(EvidenciasPort.class);
    private final SeguimientoPublicoService service = new SeguimientoPublicoService(enlaces, alertas, identidad, ajustes, evidencias);

    private TrackingLink enlace;

    @BeforeEach
    void setUp() {
        enlace = TrackingLink.para(5L, CODIGO);
        when(enlaces.porHash(TrackingLink.hash(CODIGO))).thenReturn(Optional.of(enlace));
        when(identidad.nombreDePilaDelPerfil(10L)).thenReturn(Optional.of("Ana"));
        when(ajustes.numeroEmergencia()).thenReturn("123");
    }

    private void alerta(String estado, LocalDateTime fin) {
        when(alertas.buscar(5L)).thenReturn(Optional.of(new AlertSummary(5L, 10L, "main", "panic_button", estado, null,
                AHORA.minusHours(1), new BigDecimal("4.6"), new BigDecimal("-74.1"), fin, null)));
    }

    @Test
    void crearGuardaSoloElHashDelCodigo() {
        String codigo = service.crearEnlace(5L);

        ArgumentCaptor<TrackingLink> guardado = ArgumentCaptor.forClass(TrackingLink.class);
        verify(enlaces).guardar(guardado.capture());
        assertEquals(TrackingLink.hash(codigo), guardado.getValue().getTokenHash());
        assertEquals(5L, guardado.getValue().getAlertId());
    }

    @Test
    void consultarMuestraNombreDePilaRecorridoYGrabaciones() {
        alerta("active", null);
        LocationPoint punto = new LocationPoint(new BigDecimal("4.61"), new BigDecimal("-74.08"), null, AHORA);
        when(alertas.recorrido(5L)).thenReturn(List.of(punto));
        when(evidencias.deAlerta(5L)).thenReturn(List.of(new EvidenceSummary(9L, "video", "video/mp4", 100L, AHORA)));

        TrackingView v = service.consultar(CODIGO, AHORA);

        assertEquals("Ana", v.firstName());
        assertTrue(v.active());
        assertEquals(punto, v.lastPoint());
        assertEquals("123", v.emergencyNumber());
        assertEquals(9L, v.evidence().get(0).id());
    }

    @Test
    void sinRecorridoUsaLaUbicacionConLaQueSeActivo() {
        alerta("active", null);
        when(alertas.recorrido(5L)).thenReturn(List.of());
        when(evidencias.deAlerta(5L)).thenReturn(List.of());

        assertEquals(new BigDecimal("4.6"), service.consultar(CODIGO, AHORA).lastPoint().latitude());
    }

    @Test
    void venceDosHorasDespuesDeTerminar() {
        alerta("resolved", AHORA.minusHours(3));

        assertThrows(ResourceNotFoundException.class, () -> service.consultar(CODIGO, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.mensajes(CODIGO, 0L, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.evidencia(CODIGO, 9L, AHORA));
    }

    @Test
    void rechazaEnlacesRevocadosInexistentesOMalFormados() {
        alerta("active", null);
        enlace.setRevokedAt(AHORA);

        assertThrows(ResourceNotFoundException.class, () -> service.consultar(CODIGO, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.consultar("corto", AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.consultar(TrackingLink.generarCodigo(), AHORA));
    }

    @Test
    void laEvidenciaDeOtraAlertaNoExiste() {
        alerta("active", null);
        when(evidencias.archivo(5L, 99L)).thenReturn(Optional.empty());
        EvidenceFile archivo = new EvidenceFile("video/mp4", "v.mp4", null, Optional.empty());
        when(evidencias.archivo(5L, 9L)).thenReturn(Optional.of(archivo));

        assertSame(archivo, service.evidencia(CODIGO, 9L, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.evidencia(CODIGO, 99L, AHORA));
    }

    @Test
    void elContactoEscribeEnElChatPeroNoPuedeInundarlo() {
        alerta("active", null);
        AlertMessageView m = new AlertMessageView(1L, "contact", "Bea", "Hola", AHORA);
        when(alertas.mensajeDeContacto(5L, "Bea", "Hola")).thenReturn(m);

        assertSame(m, service.enviarMensaje(CODIGO, "Bea", "Hola", AHORA));
        assertThrows(IllegalArgumentException.class, () -> service.enviarMensaje(CODIGO, "Bea", "Hola", AHORA.plusSeconds(1)));
        verify(alertas, times(1)).mensajeDeContacto(any(), any(), any());
    }
}
