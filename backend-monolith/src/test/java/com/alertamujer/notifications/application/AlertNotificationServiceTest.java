package com.alertamujer.notifications.application;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.config.FcmService;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.notifications.application.email.EmailSender;
import com.alertamujer.settings.api.SettingsApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServiceTest {

    @Mock private AlertsApi alertsApi;
    @Mock private IdentityApi identityApi;
    @Mock private DevicesApi devicesApi;
    @Mock private SettingsApi settingsApi;
    @Mock private TrackingLinkService trackingLinkService;
    @Mock private EmailSender emailSender;
    @Mock private FcmService fcmService;

    private AlertNotificationService service;
    private final AlertActivatedEvent evento = new AlertActivatedEvent(5L, 10L, LocalDateTime.of(2026, 10, 16, 15, 0));

    @BeforeEach
    void setUp() {
        service = new AlertNotificationService(alertsApi, identityApi, devicesApi, settingsApi, trackingLinkService,
                emailSender, fcmService, "https://alerta-mujer.vercel.app/");
        lenient().when(alertsApi.findAlert(5L)).thenReturn(Optional.of(new AlertSummary(5L, 10L, "main", "panic_button", "active",
                null, evento.startedAt(), new BigDecimal("2.93"), new BigDecimal("-75.28"), null, null)));
        lenient().when(identityApi.findUserByProfileId(10L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", "Gómez", "ana@t.co", null, 1L, null)));
    }

    @Test
    void enviaCorreoConEnlaceDeSeguimientoYMarcaEnviado() {
        when(alertsApi.recipientsOf(5L)).thenReturn(List.of(new AlertRecipient(100L, 1L, "Mamá", "email", "mama@correo.com")));
        when(trackingLinkService.crear(5L)).thenReturn("CODIGO123");
        when(settingsApi.numeroEmergencia()).thenReturn("123");

        service.avisar(evento);

        ArgumentCaptor<EmailSender.EmailMessage> correo = ArgumentCaptor.forClass(EmailSender.EmailMessage.class);
        verify(emailSender).send(correo.capture());
        assertTrue(correo.getValue().subject().contains("Ana Gómez"));
        assertTrue(correo.getValue().html().contains("https://alerta-mujer.vercel.app/seguimiento/CODIGO123"));
        assertTrue(correo.getValue().text().contains("google.com/maps"));
        verify(alertsApi).markDelivery(100L, "sent", null);
    }

    @Test
    void correoFallidoQuedaRegistradoConElMotivo() {
        when(alertsApi.recipientsOf(5L)).thenReturn(List.of(new AlertRecipient(100L, 1L, "Mamá", "email", "mama@correo.com")));
        when(trackingLinkService.crear(5L)).thenReturn("CODIGO123");
        when(settingsApi.numeroEmergencia()).thenReturn("123");
        doThrow(new EmailSender.EmailException("No hay proveedor de correo configurado")).when(emailSender).send(any());

        service.avisar(evento);

        verify(alertsApi).markDelivery(100L, "failed", "No hay proveedor de correo configurado");
    }

    @Test
    void pushDeAltaPrioridadAContactosConLaApp() {
        when(alertsApi.recipientsOf(5L)).thenReturn(List.of(
                new AlertRecipient(100L, 1L, "Eva", "email", "eva@correo.com"),
                new AlertRecipient(101L, 1L, "Eva", "push", "App Alerta Mujer")));
        when(trackingLinkService.crear(5L)).thenReturn("C");
        when(settingsApi.numeroEmergencia()).thenReturn("123");
        when(identityApi.findUserByEmail("eva@correo.com")).thenReturn(Optional.of(new UserSummary(3L, "Eva", null, "eva@correo.com", null, 1L, null)));
        when(devicesApi.pushTokensOfUser(3L)).thenReturn(List.of("tok-eva"));
        when(fcmService.sendEmergencyAlert(anyList(), anyMap())).thenReturn(List.of());

        service.avisar(evento);

        verify(fcmService).sendEmergencyAlert(List.of("tok-eva"),
                Map.of("tipo", "ALERTA_SOS", "alertaId", "5", "nombre", "Ana Gómez", "codigo", "C"));
        verify(alertsApi).markDelivery(101L, "sent", null);
    }

    @Test
    void pushSinDispositivosQuedaFallido() {
        when(alertsApi.recipientsOf(5L)).thenReturn(List.of(new AlertRecipient(101L, 1L, "Eva", "push", "App Alerta Mujer")));
        when(trackingLinkService.crear(5L)).thenReturn("C");
        when(identityApi.findUserByEmail(null)).thenReturn(Optional.empty());

        service.avisar(evento);

        verify(alertsApi).markDelivery(101L, "failed", "El contacto no tiene la app con notificaciones activas");
        verifyNoInteractions(fcmService);
    }

    @Test
    void elNombreDelContactoSeEscapaEnElHtml() {
        EmailSender.EmailMessage m = AlertEmailTemplate.construir("a@b.co", "<script>x</script>", "Ana", null,
                "https://x/seguimiento/C", null, "123");

        assertTrue(m.html().contains("&lt;script&gt;"));
    }

    @Test
    void alTerminarAvisaPorCorreoYPushSinTocarLosEnvios() {
        when(alertsApi.recipientsOf(5L)).thenReturn(List.of(
                new AlertRecipient(100L, 1L, "Mamá", "email", "mama@correo.com"),
                new AlertRecipient(101L, 1L, "Mamá", "push", "app")));
        when(settingsApi.numeroEmergencia()).thenReturn("123");
        when(identityApi.findUserByEmail("mama@correo.com"))
                .thenReturn(Optional.of(new UserSummary(7L, "Rosa", "Gómez", "mama@correo.com", null, 7L, null)));
        when(devicesApi.pushTokensOfUser(7L)).thenReturn(List.of("token-mama"));

        service.avisarFin(new AlertEndedEvent(5L, 10L, "resolved", LocalDateTime.of(2026, 10, 16, 15, 20)));

        ArgumentCaptor<EmailSender.EmailMessage> correo = ArgumentCaptor.forClass(EmailSender.EmailMessage.class);
        verify(emailSender).send(correo.capture());
        assertTrue(correo.getValue().subject().contains("terminó"));
        assertTrue(correo.getValue().text().contains("atendida"));
        verify(fcmService).sendEmergencyAlert(eq(List.of("token-mama")), argThat(d -> "ALERTA_FINALIZADA".equals(d.get("tipo"))));
        verify(alertsApi, never()).markDelivery(any(), any(), any());
        verify(trackingLinkService, never()).crear(any());
    }
}
