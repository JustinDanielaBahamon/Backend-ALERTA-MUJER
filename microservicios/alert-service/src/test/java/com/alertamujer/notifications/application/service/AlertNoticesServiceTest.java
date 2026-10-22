package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase;
import com.alertamujer.notifications.application.port.out.*;
import com.alertamujer.notifications.application.port.out.EmailsPort.EmailNotSentException;
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
class AlertNoticesServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 16, 15, 0);

    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final DevicesPort devices = mock(DevicesPort.class);
    private final SettingsPort settings = mock(SettingsPort.class);
    private final PublicTrackingUseCase tracking = mock(PublicTrackingUseCase.class);
    private final EmailsPort emails = mock(EmailsPort.class);
    private final PushPort push = mock(PushPort.class);
    private final AlertNoticesService service = new AlertNoticesService(alerts, identity, devices, settings,
            tracking, emails, push, "https://web.app/");

    private static final AlertRecipient EMAIL = new AlertRecipient(100L, 1L, "Mamá", "email", "mama@correo.com");
    private static final AlertRecipient PUSH = new AlertRecipient(101L, 1L, "Mamá", "push", "App Alerta Mujer");

    @BeforeEach
    void setUp() {
        when(identity.profileFullName(10L)).thenReturn(Optional.of("Ana Gómez"));
        when(settings.emergencyNumber()).thenReturn("123");
        when(tracking.createLink(5L)).thenReturn("CODIGO");
        when(alerts.find(5L)).thenReturn(Optional.of(new AlertSummary(5L, 10L, "main", "panic_button", "active", null,
                START, new BigDecimal("4.6"), new BigDecimal("-74.1"), null, null)));
    }

    @Test
    void emailWithTrackingLinkAndGoogleMapsIsSent() {
        when(alerts.recipients(5L)).thenReturn(List.of(EMAIL));

        service.alertActivated(5L, 10L, START);

        verify(emails).alertNotice("mama@correo.com", "Mamá", "Ana Gómez", START, "https://web.app/seguimiento/CODIGO",
                "https://www.google.com/maps/search/?api=1&query=4.6,-74.1", "123");
        verify(alerts).recordDelivery(100L, "sent", null);
    }

    @Test
    void failedEmailIsRecordedWithTheReason() {
        when(alerts.recipients(5L)).thenReturn(List.of(EMAIL));
        doThrow(new EmailNotSentException("Rechazado")).when(emails)
                .alertNotice(any(), any(), any(), any(), any(), any(), any());

        service.alertActivated(5L, 10L, START);

        verify(alerts).recordDelivery(100L, "failed", "Rechazado");
    }

    @Test
    void highPriorityPushToTheContactWithTheApp() {
        when(alerts.recipients(5L)).thenReturn(List.of(EMAIL, PUSH));
        when(identity.userWithEmail("mama@correo.com")).thenReturn(Optional.of(7L));
        when(devices.tokensDe(7L)).thenReturn(List.of("token-mama"));
        when(push.send(anyList(), anyMap())).thenReturn(List.of());

        service.alertActivated(5L, 10L, START);

        verify(push).send(List.of("token-mama"),
                Map.of("tipo", "ALERTA_SOS", "alertaId", "5", "nombre", "Ana Gómez", "codigo", "CODIGO"));
        verify(alerts).recordDelivery(101L, "sent", null);
        verify(tracking, times(1)).createLink(5L);
    }

    @Test
    void pushWithoutDevicesOrAllRejectedIsFailed() {
        when(alerts.recipients(5L)).thenReturn(List.of(EMAIL, PUSH));
        when(identity.userWithEmail("mama@correo.com")).thenReturn(Optional.empty());

        service.alertActivated(5L, 10L, START);
        verify(alerts).recordDelivery(101L, "failed", "El contacto no tiene la app con notificaciones activas");

        when(identity.userWithEmail("mama@correo.com")).thenReturn(Optional.of(7L));
        when(devices.tokensDe(7L)).thenReturn(List.of("t1"));
        when(push.send(anyList(), anyMap())).thenReturn(List.of("t1"));
        service.alertActivated(5L, 10L, START);
        verify(alerts).recordDelivery(101L, "failed", "FCM rechazó el envío a todos los dispositivos");
    }

    @Test
    void withoutRecipientsCreatesNoLinkAndSendsNothing() {
        when(alerts.recipients(5L)).thenReturn(List.of());

        service.alertActivated(5L, 10L, START);

        verifyNoInteractions(tracking, emails, push);
    }

    @Test
    void onEndNotifiesByEmailAndPushWithoutTouchingTheDeliveries() {
        LocalDateTime end = START.plusMinutes(20);
        when(alerts.recipients(5L)).thenReturn(List.of(EMAIL, PUSH));
        when(identity.userWithEmail("mama@correo.com")).thenReturn(Optional.of(7L));
        when(devices.tokensDe(7L)).thenReturn(List.of("token-mama"));
        doThrow(new EmailNotSentException("Sin proveedor")).when(emails).alertEnd(any(), any(), any(), any(), any(), any());

        service.alertEnded(5L, 10L, "resolved", end);

        verify(emails).alertEnd("mama@correo.com", "Mamá", "Ana Gómez", "resolved", end, "123");
        verify(push).send(eq(List.of("token-mama")), argThat(d -> "ALERTA_FINALIZADA".equals(d.get("tipo"))));
        verify(alerts, never()).recordDelivery(any(), any(), any());
        verifyNoInteractions(tracking);
    }

    @Test
    void theMapsLinkUsesCoordinatesWithoutScientificNotation() {
        assertEquals("https://www.google.com/maps/search/?api=1&query=0.000001,-74.1",
                AlertNoticesService.googleMaps(new BigDecimal("1E-6"), new BigDecimal("-74.1")));
    }
}
