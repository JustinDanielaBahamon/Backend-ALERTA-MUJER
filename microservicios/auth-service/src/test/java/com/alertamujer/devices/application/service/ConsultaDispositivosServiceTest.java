package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.IdentidadPort;
import com.alertamujer.devices.domain.model.Device;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaDispositivosServiceTest {

    private final DispositivoRepositorio repositorio = mock(DispositivoRepositorio.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final ConsultaDispositivosService service = new ConsultaDispositivosService(repositorio, identidad);

    private static Device dispositivo(String os, String token) {
        Device d = new Device();
        d.setOsName(os);
        d.setFcmToken(token);
        return d;
    }

    @Test
    void losTokensDelPerfilSeBuscanPorLaCuentaDeSuDuena() {
        when(identidad.usuariaDelPerfil(10L)).thenReturn(Optional.of(1L));
        when(identidad.cuentaDe(1L)).thenReturn(Optional.of(4L));
        when(repositorio.deCuenta(4L)).thenReturn(List.of(dispositivo("android", "t1"), dispositivo("web", "t2"), dispositivo("android", " ")));

        assertEquals(List.of("t1", "t2"), service.tokensDelPerfil(10L));
        assertEquals(List.of("t1"), service.tokensDeCelulares(1L));
    }

    @Test
    void sinPersonaOSinCuentaNoHayTokens() {
        when(identidad.usuariaDelPerfil(10L)).thenReturn(Optional.empty());
        when(identidad.cuentaDe(1L)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.tokensDelPerfil(10L));
        assertEquals(List.of(), service.tokensDeUsuaria(1L));
        assertEquals(List.of(), service.tokensDeCelulares(1L));
        assertTrue(service.esElCelularMasReciente(1L, 5L));
    }
}
