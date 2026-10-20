package com.alertamujer.alerts.application.service;

import com.alertamujer.config.FcmService;
import com.alertamujer.devices.api.DevicesApi;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class PhoneLocationRequestServiceTest {

    private final DevicesApi devicesApi = mock(DevicesApi.class);
    private final FcmService fcmService = mock(FcmService.class);
    private final PhoneLocationRequestService service = new PhoneLocationRequestService(devicesApi, fcmService,
            Clock.fixed(Instant.parse("2026-10-16T15:00:00Z"), ZoneOffset.UTC));

    @Test
    void mandaUnPushSilencioso() {
        when(devicesApi.phonePushTokensOfUser(1L)).thenReturn(List.of("t1"));
        when(fcmService.sendEmergencyAlert(anyList(), anyMap())).thenReturn(List.of());

        assertEquals(1, service.pedir(1L).celulares());
        verify(fcmService).sendEmergencyAlert(List.of("t1"), Map.of("tipo", "PEDIR_UBICACION"));
    }

    @Test
    void sinCelularConNotificacionesAvisa() {
        when(devicesApi.phonePushTokensOfUser(1L)).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> service.pedir(1L));
        verifyNoInteractions(fcmService);
    }

    @Test
    void noRepiteElPedidoEnPocosSegundos() {
        when(devicesApi.phonePushTokensOfUser(1L)).thenReturn(List.of("t1"));
        when(fcmService.sendEmergencyAlert(anyList(), anyMap())).thenReturn(List.of());
        service.pedir(1L);
        assertThrows(IllegalStateException.class, () -> service.pedir(1L));
    }
}
