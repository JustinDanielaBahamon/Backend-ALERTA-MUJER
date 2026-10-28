package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.Device;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceQueryServiceTest {

    private final DeviceRepository repository = mock(DeviceRepository.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final DeviceQueryService service = new DeviceQueryService(repository, identity);

    private static Device device(String os, String token) {
        Device d = new Device();
        d.setOsName(os);
        d.setFcmToken(token);
        return d;
    }

    @Test
    void theProfileTokensAreFoundThroughItsOwnerAccount() {
        when(identity.userOfProfile(10L)).thenReturn(Optional.of(1L));
        when(identity.accountOf(1L)).thenReturn(Optional.of(4L));
        when(repository.ofAccount(4L)).thenReturn(List.of(device("android", "t1"), device("web", "t2"), device("android", " ")));

        assertEquals(List.of("t1", "t2"), service.profileTokens(10L));
        assertEquals(List.of("t1"), service.phoneTokens(1L));
    }

    @Test
    void withoutPersonOrAccountThereAreNoTokens() {
        when(identity.userOfProfile(10L)).thenReturn(Optional.empty());
        when(identity.accountOf(1L)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.profileTokens(10L));
        assertEquals(List.of(), service.userTokens(1L));
        assertEquals(List.of(), service.phoneTokens(1L));
        assertTrue(service.isTheNewestPhone(1L, 5L));
    }
}
