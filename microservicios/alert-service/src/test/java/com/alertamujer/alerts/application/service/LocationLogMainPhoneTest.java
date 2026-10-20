package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.controller.dto.LocationLogRequest;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.identity.api.IdentityApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LocationLogMainPhoneTest {

    private final LocationLogRepository logs = mock(LocationLogRepository.class);
    private final IdentityApi identityApi = mock(IdentityApi.class);
    private final AlertRepository alerts = mock(AlertRepository.class);
    private final DevicesApi devicesApi = mock(DevicesApi.class);
    private final LocationLogService service = new LocationLogService(logs, identityApi, alerts, devicesApi);

    @BeforeEach
    void preparar() {
        when(identityApi.requireProfileId(1L)).thenReturn(70L);
        when(logs.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(devicesApi.isMostRecentPhone(1L, 3L)).thenReturn(false);
    }

    private LocationLogRequest punto(Long alertId) {
        LocationLogRequest r = new LocationLogRequest();
        r.setLatitude(BigDecimal.valueOf(2.93));
        r.setLongitude(BigDecimal.valueOf(-75.28));
        r.setDeviceId(3L);
        r.setAlertId(alertId);
        return r;
    }

    @Test
    void descartaLaUbicacionDiariaDeUnCelularQueNoEsElPrincipal() {
        assertNull(service.saveLocation(punto(null), 1L));
        verify(logs, never()).save(any());
    }

    @Test
    void laUbicacionDeUnaAlertaSeGuardaVengaDelCelularQueVenga() {
        Alert alerta = new Alert();
        alerta.setId(9L);
        alerta.setUserProfileId(70L);
        when(alerts.findById(9L)).thenReturn(Optional.of(alerta));

        assertNotNull(service.saveLocation(punto(9L), 1L));
    }
}
