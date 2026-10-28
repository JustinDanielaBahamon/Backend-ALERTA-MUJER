package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.ManageDevicesUseCase.DeviceWithOwner;
import com.alertamujer.devices.application.port.out.DeviceRepository;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeviceAdministrationServiceTest {

    private final DeviceRepository repository = mock(DeviceRepository.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final DeviceAdministrationService service = new DeviceAdministrationService(repository, identity);

    private static Device device(Long id, LocalDateTime access) {
        Device d = new Device();
        d.setId(id);
        d.setAccountId(4L);
        d.setLastAccess(access);
        return d;
    }

    @Test
    void listsNewestToOldestWithItsOwner() {
        LocalDateTime now = LocalDateTime.now();
        UserSummary ana = new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
        when(repository.all()).thenReturn(List.of(device(1L, now.minusDays(1)), device(2L, null), device(3L, now)));
        when(identity.ownersByAccount()).thenReturn(Map.of(4L, ana));

        List<DeviceWithOwner> r = service.listAll();

        assertEquals(List.of(3L, 1L, 2L), r.stream().map(dc -> dc.device().getId()).toList());
        assertSame(ana, r.get(0).owner());
    }

    @Test
    void changeStatusValidatesAndSaves() {
        Device d = device(1L, null);
        when(repository.find(1L)).thenReturn(Optional.of(d));
        when(repository.find(9L)).thenReturn(Optional.empty());

        assertEquals("lost", service.changeStatus(1L, "lost").device().getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(1L, "roto"));
        assertThrows(ResourceNotFoundException.class, () -> service.changeStatus(9L, "lost"));
        verify(repository, times(1)).save(any());
    }
}
