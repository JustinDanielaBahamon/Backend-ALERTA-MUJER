package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.EventosDispositivosPort;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.DatosDispositivo;
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
class MisDispositivosServiceTest {

    private final DispositivoRepositorio repository = mock(DispositivoRepositorio.class);
    private final IdentityPort identidad = mock(IdentityPort.class);
    private final EventosDispositivosPort events = mock(EventosDispositivosPort.class);
    private final MisDispositivosService service = new MisDispositivosService(repository, identidad, events);

    @BeforeEach
    void setUp() {
        when(identidad.cuentaRequerida(1L)).thenReturn(4L);
        when(identidad.cuentaRequerida(2L)).thenReturn(5L);
        when(repository.save(any(Device.class))).thenAnswer(inv -> {
            Device d = inv.getArgument(0);
            if (d.getId() == null) d.setId(30L);
            return d;
        });
    }

    private static Device propio(Long id, String os, String token) {
        Device d = new Device();
        d.setId(id);
        d.setAccountId(4L);
        d.setOsName(os);
        d.setFcmToken(token);
        return d;
    }

    private static DatosDispositivo celular(String name) {
        return new DatosDispositivo("uuid", "Xiaomi", "Note 13", name, "android", null, null, null, null, null, "t-nuevo", null);
    }

    @Test
    void alEntrarDesdeOtroCelularAvisaSoloALosDemasCelularesConToken() {
        when(repository.deCuenta(4L)).thenReturn(List.of(propio(10L, "android", "t-viejo"), propio(11L, "web", "t-web"),
                propio(12L, "android", null), propio(30L, "android", "t-nuevo")));

        Device d = service.registrar(celular("Mi Xiaomi"), 1L);

        assertEquals(4L, d.getAccountId());
        verify(events).nuevoCelular("Mi Xiaomi", List.of("t-viejo"));
    }

    @Test
    void elMismoCelularReinstaladoReutilizaSuRegistroSinAvisar() {
        Device existente = propio(10L, "android", "t-viejo");
        existente.setBrand("xiaomi");
        existente.setModel("Note 13");
        existente.setDeviceName("Mi Xiaomi");
        when(repository.deCuenta(4L)).thenReturn(List.of(existente, propio(11L, "android", "t-otro")));

        Device d = service.registrar(celular("Mi Xiaomi"), 1L);

        assertEquals(10L, d.getId(), "no se crea un registro nuevo");
        verify(repository, never()).save(argThat((Device x) -> Long.valueOf(30L).equals(x.getId())));
        verifyNoInteractions(events);
    }

    @Test
    void elPanelWebOElPrimerCelularNoAvisan() {
        when(repository.deCuenta(4L)).thenReturn(List.of(propio(30L, "android", "t-nuevo")));
        service.registrar(celular(null), 1L);

        service.registrar(new DatosDispositivo("uuid", null, null, null, "web", null, null, null, null, null, null, null), 1L);

        verifyNoInteractions(events);
    }

    @Test
    void noSePuedeTocarElDispositivoDeOtraCuenta() {
        when(repository.find(10L)).thenReturn(Optional.of(propio(10L, "android", "t")));

        assertThrows(AccessDeniedException.class, () -> service.uno(10L, 2L));
        assertThrows(AccessDeniedException.class, () -> service.registrarUso(10L, celular("x"), 2L));
        assertThrows(AccessDeniedException.class, () -> service.cambiarTokenPush(10L, "t2", 2L));
        assertThrows(AccessDeniedException.class, () -> service.delete(10L, 2L));
        verify(repository, never()).delete(any());
    }

    @Test
    void laDuenaCambiaElTokenPush() {
        Device d = propio(10L, "android", "t");
        when(repository.find(10L)).thenReturn(Optional.of(d));

        service.cambiarTokenPush(10L, "t2", 1L);

        assertEquals("t2", d.getFcmToken());
        verify(repository).save(d);
    }
}
