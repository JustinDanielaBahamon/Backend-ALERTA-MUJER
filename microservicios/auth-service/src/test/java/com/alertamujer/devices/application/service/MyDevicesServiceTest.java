package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.application.port.out.DeviceEventsPort;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.DeviceData;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Dispositivos de la usuaria con los puertos simulados. */
class MyDevicesServiceTest {

    private final DeviceRepository repository = mock(DeviceRepository.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final DeviceEventsPort events = mock(DeviceEventsPort.class);
    private final MyDevicesService service = new MyDevicesService(repository, identity, events);

    @BeforeEach
    void setUp() {
        when(identity.requiredAccount(1L)).thenReturn(4L);
        when(identity.requiredAccount(2L)).thenReturn(5L);
        when(repository.save(any(Device.class))).thenAnswer(inv -> {
            Device d = inv.getArgument(0);
            if (d.getId() == null) d.setId(30L);
            return d;
        });
    }

    private static Device own(Long id, String os, String token) {
        Device d = new Device();
        d.setId(id);
        d.setAccountId(4L);
        d.setOsName(os);
        d.setFcmToken(token);
        return d;
    }

    private static DeviceData phone(String name) {
        return new DeviceData("uuid", "Xiaomi", "Note 13", name, "android", null, null, null, null, null, "t-nuevo", null);
    }

    @Test
    void signingInFromAnotherPhoneNotifiesOnlyTheOtherPhonesWithToken() {
        when(repository.ofAccount(4L)).thenReturn(List.of(own(10L, "android", "t-viejo"), own(11L, "web", "t-web"),
                own(12L, "android", null), own(30L, "android", "t-nuevo")));

        Device d = service.registrar(phone("Mi Xiaomi"), 1L);

        assertEquals(4L, d.getAccountId());
        verify(events).newPhone("Mi Xiaomi", List.of("t-viejo"));
    }

    @Test
    void theSameReinstalledPhoneReusesItsRecordWithoutNotifying() {
        Device existing = own(10L, "android", "t-viejo");
        existing.setBrand("xiaomi");
        existing.setModel("Note 13");
        existing.setDeviceName("Mi Xiaomi");
        when(repository.ofAccount(4L)).thenReturn(List.of(existing, own(11L, "android", "t-otro")));

        Device d = service.registrar(phone("Mi Xiaomi"), 1L);

        assertEquals(10L, d.getId(), "no se crea un registro nuevo");
        verify(repository, never()).save(argThat((Device x) -> Long.valueOf(30L).equals(x.getId())));
        verifyNoInteractions(events);
    }

    @Test
    void theWebPanelOrTheFirstPhoneDoNotNotify() {
        when(repository.ofAccount(4L)).thenReturn(List.of(own(30L, "android", "t-nuevo")));
        service.registrar(phone(null), 1L);

        service.registrar(new DeviceData("uuid", null, null, null, "web", null, null, null, null, null, null, null), 1L);

        verifyNoInteractions(events);
    }

    @Test
    void cannotTouchAnotherAccountsDevice() {
        when(repository.find(10L)).thenReturn(Optional.of(own(10L, "android", "t")));

        assertThrows(AccessDeniedException.class, () -> service.one(10L, 2L));
        assertThrows(AccessDeniedException.class, () -> service.recordUse(10L, phone("x"), 2L));
        assertThrows(AccessDeniedException.class, () -> service.changePushToken(10L, "t2", 2L));
        assertThrows(AccessDeniedException.class, () -> service.delete(10L, 2L));
        verify(repository, never()).delete(any());
    }

    @Test
    void theOwnerChangesThePushToken() {
        Device d = own(10L, "android", "t");
        when(repository.find(10L)).thenReturn(Optional.of(d));

        service.changePushToken(10L, "t2", 1L);

        assertEquals("t2", d.getFcmToken());
        verify(repository).save(d);
    }
}
