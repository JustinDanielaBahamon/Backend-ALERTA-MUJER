package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.out.*;
import com.alertamujer.notifications.application.port.out.CorreosPort.CorreoNoEnviadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Avisos a los contactos con los puertos simulados (sin correo, Firebase ni otros servicios). */
class AvisosAlertaServiceTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 16, 15, 0);

    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identidad = mock(IdentityPort.class);
    private final DispositivosPort dispositivos = mock(DispositivosPort.class);
    private final AjustesPort ajustes = mock(AjustesPort.class);
    private final SeguimientoPublicoUseCase seguimiento = mock(SeguimientoPublicoUseCase.class);
    private final CorreosPort correos = mock(CorreosPort.class);
    private final PushPort push = mock(PushPort.class);
    private final AvisosAlertaService service = new AvisosAlertaService(alerts, identidad, dispositivos, ajustes,
            seguimiento, correos, push, "https://web.app/");

    private static final AlertRecipient CORREO = new AlertRecipient(100L, 1L, "Mamá", "email", "mama@correo.com");
    private static final AlertRecipient PUSH = new AlertRecipient(101L, 1L, "Mamá", "push", "App Alerta Mujer");

    @BeforeEach
    void setUp() {
        when(identidad.nombreCompletoDelPerfil(10L)).thenReturn(Optional.of("Ana Gómez"));
        when(ajustes.numeroEmergencia()).thenReturn("123");
        when(seguimiento.crearEnlace(5L)).thenReturn("CODIGO");
        when(alerts.find(5L)).thenReturn(Optional.of(new AlertSummary(5L, 10L, "main", "panic_button", "active", null,
                INICIO, new BigDecimal("4.6"), new BigDecimal("-74.1"), null, null)));
    }

    @Test
    void correoConEnlaceDeSeguimientoYGoogleMapsQuedaEnviado() {
        when(alerts.destinatarios(5L)).thenReturn(List.of(CORREO));

        service.alertaActivada(5L, 10L, INICIO);

        verify(correos).avisoDeAlerta("mama@correo.com", "Mamá", "Ana Gómez", INICIO, "https://web.app/seguimiento/CODIGO",
                "https://www.google.com/maps/search/?api=1&query=4.6,-74.1", "123");
        verify(alerts).registrarEntrega(100L, "sent", null);
    }

    @Test
    void correoFallidoQuedaRegistradoConElMotivo() {
        when(alerts.destinatarios(5L)).thenReturn(List.of(CORREO));
        doThrow(new CorreoNoEnviadoException("Rechazado")).when(correos)
                .avisoDeAlerta(any(), any(), any(), any(), any(), any(), any());

        service.alertaActivada(5L, 10L, INICIO);

        verify(alerts).registrarEntrega(100L, "failed", "Rechazado");
    }

    @Test
    void pushDeAltaPrioridadAlContactoConLaApp() {
        when(alerts.destinatarios(5L)).thenReturn(List.of(CORREO, PUSH));
        when(identidad.usuariaConCorreo("mama@correo.com")).thenReturn(Optional.of(7L));
        when(dispositivos.tokensDe(7L)).thenReturn(List.of("token-mama"));
        when(push.enviar(anyList(), anyMap())).thenReturn(List.of());

        service.alertaActivada(5L, 10L, INICIO);

        verify(push).enviar(List.of("token-mama"),
                Map.of("tipo", "ALERTA_SOS", "alertaId", "5", "nombre", "Ana Gómez", "codigo", "CODIGO"));
        verify(alerts).registrarEntrega(101L, "sent", null);
        verify(seguimiento, times(1)).crearEnlace(5L);
    }

    @Test
    void pushSinDispositivosOConTodosRechazadosQuedaFallido() {
        when(alerts.destinatarios(5L)).thenReturn(List.of(CORREO, PUSH));
        when(identidad.usuariaConCorreo("mama@correo.com")).thenReturn(Optional.empty());

        service.alertaActivada(5L, 10L, INICIO);
        verify(alerts).registrarEntrega(101L, "failed", "El contacto no tiene la app con notificaciones activas");

        when(identidad.usuariaConCorreo("mama@correo.com")).thenReturn(Optional.of(7L));
        when(dispositivos.tokensDe(7L)).thenReturn(List.of("t1"));
        when(push.enviar(anyList(), anyMap())).thenReturn(List.of("t1"));
        service.alertaActivada(5L, 10L, INICIO);
        verify(alerts).registrarEntrega(101L, "failed", "FCM rechazó el envío a todos los dispositivos");
    }

    @Test
    void sinDestinatariosNoCreaEnlaceNiEnviaNada() {
        when(alerts.destinatarios(5L)).thenReturn(List.of());

        service.alertaActivada(5L, 10L, INICIO);

        verifyNoInteractions(seguimiento, correos, push);
    }

    @Test
    void alTerminarAvisaPorCorreoYPushSinTocarLosEnvios() {
        LocalDateTime fin = INICIO.plusMinutes(20);
        when(alerts.destinatarios(5L)).thenReturn(List.of(CORREO, PUSH));
        when(identidad.usuariaConCorreo("mama@correo.com")).thenReturn(Optional.of(7L));
        when(dispositivos.tokensDe(7L)).thenReturn(List.of("token-mama"));
        doThrow(new CorreoNoEnviadoException("Sin proveedor")).when(correos).finDeAlerta(any(), any(), any(), any(), any(), any());

        service.alertaTerminada(5L, 10L, "resolved", fin);

        verify(correos).finDeAlerta("mama@correo.com", "Mamá", "Ana Gómez", "resolved", fin, "123");
        verify(push).enviar(eq(List.of("token-mama")), argThat(d -> "ALERTA_FINALIZADA".equals(d.get("tipo"))));
        verify(alerts, never()).registrarEntrega(any(), any(), any());
        verifyNoInteractions(seguimiento);
    }

    @Test
    void elEnlaceDeMapsUsaLasCoordenadasSinNotacionCientifica() {
        assertEquals("https://www.google.com/maps/search/?api=1&query=0.000001,-74.1",
                AvisosAlertaService.googleMaps(new BigDecimal("1E-6"), new BigDecimal("-74.1")));
    }
}
