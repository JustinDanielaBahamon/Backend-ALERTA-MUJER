package com.alertamujer.devices.application.service;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeviceServiceTest {

    private final DeviceRepository repository = mock(DeviceRepository.class);
    private final IdentityApi identityApi = mock(IdentityApi.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final DeviceService service = new DeviceService(repository, identityApi, events);
    private final DevicesApiService api = new DevicesApiService(repository, identityApi);

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

    @Test
    void guardaLosPermisosQueInformaLaApp() {
        Device existente = celular();
        when(identityApi.requireAccountId(1L)).thenReturn(7L);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Device datos = new Device();
        datos.setPermissions("{\"ubicacion\": true, \"agitar\": false}");
        Device guardado = service.registrarUso(3L, datos, 1L);

        assertEquals("{\"ubicacion\":true,\"agitar\":false}", guardado.getPermissions());
        assertNotNull(guardado.getPermissionsUpdatedAt());
    }

    @Test
    void rechazaPermisosQueNoSonSiONo() {
        assertThrows(IllegalArgumentException.class, () -> DeviceService.validarPermisos("{\"ubicacion\":\"<script>\"}"));
        assertThrows(IllegalArgumentException.class, () -> DeviceService.validarPermisos("[true]"));
        assertEquals("{}", DeviceService.validarPermisos("{}"));
    }

    private Device otro(long id, String os, String token, LocalDateTime acceso) {
        Device d = new Device();
        d.setId(id);
        d.setAccountId(7L);
        d.setOsName(os);
        d.setFcmToken(token);
        d.setLastAccess(acceso);
        return d;
    }

    @Test
    void alEntrarDesdeOtroCelularAvisaALosDemasCelulares() {
        when(identityApi.requireAccountId(1L)).thenReturn(7L);
        when(repository.save(any())).thenAnswer(inv -> {
            Device d = inv.getArgument(0);
            d.setId(9L);
            return d;
        });
        Device nuevo = new Device();
        nuevo.setOsName("android");
        nuevo.setBrand("Samsung");
        nuevo.setModel("A54");
        when(repository.findByAccountId(7L)).thenReturn(List.of(
                otro(3L, "android", "token-viejo", null), otro(4L, "web", "token-web", null), nuevo));

        service.createDevice(nuevo, 1L);

        verify(events).publishEvent(new NewPhoneSignedInEvent("Samsung A54", List.of("token-viejo")));
    }

    @Test
    void laUbicacionDelDiaADiaSoloSeTomaDelCelularMasReciente() {
        when(identityApi.requireAccountId(1L)).thenReturn(7L);
        LocalDateTime ahora = LocalDateTime.now();
        when(repository.findByAccountId(7L)).thenReturn(List.of(
                otro(3L, "android", null, ahora.minusDays(2)),
                otro(5L, "android", null, ahora),
                otro(6L, "web", null, ahora.plusMinutes(5))));

        assertTrue(api.isMostRecentPhone(1L, 5L));
        assertFalse(api.isMostRecentPhone(1L, 3L));
        assertTrue(api.isMostRecentPhone(1L, null)); // versiones anteriores de la app
        assertTrue(api.isMostRecentPhone(1L, 99L));
    }
}
