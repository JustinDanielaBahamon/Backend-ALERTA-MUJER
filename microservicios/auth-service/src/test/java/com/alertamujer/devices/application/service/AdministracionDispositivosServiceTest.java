package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.AdministrarDispositivosUseCase.DispositivoConDuena;
import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.IdentidadPort;
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

class AdministracionDispositivosServiceTest {

    private final DispositivoRepositorio repositorio = mock(DispositivoRepositorio.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final AdministracionDispositivosService service = new AdministracionDispositivosService(repositorio, identidad);

    private static Device dispositivo(Long id, LocalDateTime acceso) {
        Device d = new Device();
        d.setId(id);
        d.setAccountId(4L);
        d.setLastAccess(acceso);
        return d;
    }

    @Test
    void listaDelMasRecienteAlMasAntiguoConSuDuena() {
        LocalDateTime ahora = LocalDateTime.now();
        UserSummary ana = new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
        when(repositorio.todos()).thenReturn(List.of(dispositivo(1L, ahora.minusDays(1)), dispositivo(2L, null), dispositivo(3L, ahora)));
        when(identidad.duenasPorCuenta()).thenReturn(Map.of(4L, ana));

        List<DispositivoConDuena> r = service.listar();

        assertEquals(List.of(3L, 1L, 2L), r.stream().map(dc -> dc.dispositivo().getId()).toList());
        assertSame(ana, r.get(0).duena());
    }

    @Test
    void cambiarEstadoValidaYGuarda() {
        Device d = dispositivo(1L, null);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(d));
        when(repositorio.buscar(9L)).thenReturn(Optional.empty());

        assertEquals("lost", service.cambiarEstado(1L, "lost").dispositivo().getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(1L, "roto"));
        assertThrows(ResourceNotFoundException.class, () -> service.cambiarEstado(9L, "lost"));
        verify(repositorio, times(1)).guardar(any());
    }
}
