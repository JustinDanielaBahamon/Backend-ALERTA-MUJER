package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeviceServiceTest {

    private final DeviceRepository repository = mock(DeviceRepository.class);
    private final IdentityApi identityApi = mock(IdentityApi.class);
    private final DeviceService service = new DeviceService(repository, identityApi);

    private Device celular() {
        Device d = new Device();
        d.setId(3L);
        d.setAccountId(7L);
        d.setDeviceUuid("uuid-1");
        d.setBrand("Android");
        d.setModel("Dispositivo Expo");
        d.setOsName("android");
        d.setFcmToken("token");
        return d;
    }

    @Test
    void registrarUsoActualizaSoloLoQueLlegaYMarcaLaConexion() {
        Device existente = celular();
        when(identityApi.requireAccountId(1L)).thenReturn(7L);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Device datos = new Device();
        datos.setBrand("samsung");
        datos.setModel("Galaxy A54");
        datos.setDeviceName("Galaxy de Ana");
        datos.setOsVersion("14");
        Device r = service.registrarUso(3L, datos, 1L);

        assertEquals("Galaxy A54", r.getModel());
        assertEquals("Galaxy de Ana", r.getDeviceName());
        assertEquals("14", r.getOsVersion());
        assertEquals("android", r.getOsName()); // no vino: se conserva
        assertEquals("token", r.getFcmToken());
        assertNotNull(r.getLastAccess());
    }

    @Test
    void noSePuedeActualizarElCelularDeOtraCuenta() {
        when(identityApi.requireAccountId(2L)).thenReturn(99L);
        when(repository.findById(3L)).thenReturn(Optional.of(celular()));

        assertThrows(AccessDeniedException.class, () -> service.registrarUso(3L, new Device(), 2L));
        verify(repository, never()).save(any());
    }
}
