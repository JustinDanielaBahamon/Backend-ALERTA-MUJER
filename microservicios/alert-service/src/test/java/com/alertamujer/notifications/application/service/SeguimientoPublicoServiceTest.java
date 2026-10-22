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

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);
    private static final String CODIGO = TrackingLink.generarCodigo();

    private final EnlaceRepositorio enlaces = mock(EnlaceRepositorio.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final AjustesPort ajustes = mock(AjustesPort.class);
    private final EvidenciasPort evidences = mock(EvidenciasPort.class);
    private final VistasRepositorio views = mock(VistasRepositorio.class);
    private final SeguimientoPublicoService service = new SeguimientoPublicoService(enlaces, alerts, identity, ajustes, evidences, views);

    private TrackingLink enlace;

    @BeforeEach
    void setUp() {
        enlace = TrackingLink.para(5L, CODIGO);
        when(enlaces.porHash(TrackingLink.hash(CODIGO))).thenReturn(Optional.of(enlace));
        when(identity.nombreDePilaDelPerfil(10L)).thenReturn(Optional.of("Ana"));
        when(ajustes.emergencyNumber()).thenReturn("123");
    }

    private void alert(String status, LocalDateTime end) {
        when(alerts.find(5L)).thenReturn(Optional.of(new AlertSummary(5L, 10L, "main", "panic_button", status, null,
                NOW.minusHours(1), new BigDecimal("4.6"), new BigDecimal("-74.1"), end, null)));
    }

    @Test
    void crearGuardaSoloElHashDelCodigo() {
        String code = service.crearEnlace(5L);

        ArgumentCaptor<TrackingLink> stored = ArgumentCaptor.forClass(TrackingLink.class);
        verify(enlaces).save(stored.capture());
        assertEquals(TrackingLink.hash(code), stored.getValue().getTokenHash());
        assertEquals(5L, stored.getValue().getAlertId());
    }

    @Test
    void consultarMuestraNombreDePilaRecorridoYGrabaciones() {
        alert("active", null);
        LocationPoint dot = new LocationPoint(new BigDecimal("4.61"), new BigDecimal("-74.08"), null, NOW);
        when(alerts.recorrido(5L)).thenReturn(List.of(dot));
        when(evidences.deAlerta(5L)).thenReturn(List.of(new EvidenceSummary(9L, "video", "video/mp4", 100L, NOW)));

        TrackingView v = service.query(CODIGO, NOW);

        assertEquals("Ana", v.firstName());
        assertTrue(v.active());
        assertEquals(dot, v.lastPoint());
        assertEquals("123", v.emergencyNumber());
        assertEquals(9L, v.evidence().get(0).id());
    }

    @Test
    void sinRecorridoUsaLaUbicacionConLaQueSeActivo() {
        alert("active", null);
        when(alerts.recorrido(5L)).thenReturn(List.of());
        when(evidences.deAlerta(5L)).thenReturn(List.of());

        assertEquals(new BigDecimal("4.6"), service.query(CODIGO, NOW).lastPoint().latitude());
    }

    @Test
    void venceDosHorasDespuesDeTerminar() {
        alert("resolved", NOW.minusHours(3));

        assertThrows(ResourceNotFoundException.class, () -> service.query(CODIGO, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.mensajes(CODIGO, 0L, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.evidence(CODIGO, 9L, NOW));
    }

    @Test
    void rechazaEnlacesRevocadosInexistentesOMalFormados() {
        alert("active", null);
        enlace.setRevokedAt(NOW);

        assertThrows(ResourceNotFoundException.class, () -> service.query(CODIGO, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.query("corto", NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.query(TrackingLink.generarCodigo(), NOW));
    }

    @Test
    void laEvidenciaDeOtraAlertaNoExiste() {
        alert("active", null);
        when(evidences.file(5L, 99L)).thenReturn(Optional.empty());
        EvidenceFile file = new EvidenceFile("video/mp4", "v.mp4", null, Optional.empty());
        when(evidences.file(5L, 9L)).thenReturn(Optional.of(file));

        assertSame(file, service.evidence(CODIGO, 9L, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.evidence(CODIGO, 99L, NOW));
    }

    @Test
    void elContactoEscribeEnElChatPeroNoPuedeInundarlo() {
        alert("active", null);
        AlertMessageView m = new AlertMessageView(1L, "contact", "Bea", "Hola", NOW);
        when(alerts.mensajeDeContacto(5L, "Bea", "Hola")).thenReturn(m);

        assertSame(m, service.enviarMensaje(CODIGO, "Bea", "Hola", NOW));
        assertThrows(IllegalArgumentException.class, () -> service.enviarMensaje(CODIGO, "Bea", "Hola", NOW.plusSeconds(1)));
        verify(alerts, times(1)).mensajeDeContacto(any(), any(), any());
    }
}
