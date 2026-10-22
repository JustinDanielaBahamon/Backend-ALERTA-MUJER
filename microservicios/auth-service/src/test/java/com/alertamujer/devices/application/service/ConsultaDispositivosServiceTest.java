package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.Device;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaDispositivosServiceTest {

    private final DispositivoRepositorio repository = mock(DispositivoRepositorio.class);
    private final IdentityPort identidad = mock(IdentityPort.class);
    private final ConsultaDispositivosService service = new ConsultaDispositivosService(repository, identidad);

    private static Device device(String os, String token) {
        Device d = new Device();
        d.setOsName(os);
        d.setFcmToken(token);
        return d;
    }

    @Test
    void losTokensDelPerfilSeBuscanPorLaCuentaDeSuDuena() {
        when(identidad.userOfProfile(10L)).thenReturn(Optional.of(1L));
        when(identidad.cuentaDe(1L)).thenReturn(Optional.of(4L));
        when(repository.deCuenta(4L)).thenReturn(List.of(device("android", "t1"), device("web", "t2"), device("android", " ")));

        assertEquals(List.of("t1", "t2"), service.tokensDelPerfil(10L));
        assertEquals(List.of("t1"), service.tokensDeCelulares(1L));
    }

    @Test
    void sinPersonaOSinCuentaNoHayTokens() {
        when(identidad.userOfProfile(10L)).thenReturn(Optional.empty());
        when(identidad.cuentaDe(1L)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.tokensDelPerfil(10L));
        assertEquals(List.of(), service.tokensDeUsuaria(1L));
        assertEquals(List.of(), service.tokensDeCelulares(1L));
        assertTrue(service.esElCelularMasReciente(1L, 5L));
    }
}
