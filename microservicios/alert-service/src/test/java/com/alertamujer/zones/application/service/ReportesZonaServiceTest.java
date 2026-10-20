package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.out.IdentidadPort;
import com.alertamujer.zones.application.port.out.ReporteZonaRepositorio;
import com.alertamujer.zones.application.port.out.ZonaRepositorio;
import com.alertamujer.zones.domain.model.Zone;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** La autora del reporte es el perfil del token (antes se comparaba con users.id, que no coincide). */
class ReportesZonaServiceTest {

    private final ReporteZonaRepositorio repositorio = mock(ReporteZonaRepositorio.class);
    private final ZonaRepositorio zonas = mock(ZonaRepositorio.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final ReportesZonaService service = new ReportesZonaService(repositorio, zonas, identidad);

    @BeforeEach
    void setUp() {
        when(identidad.perfilRequerido(1L)).thenReturn(10L);
        when(identidad.perfilDe(1L)).thenReturn(Optional.of(10L));
        when(identidad.perfilRequerido(2L)).thenReturn(20L);
        when(repositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crearUsaElPerfilDelTokenYExigeUnaZonaExistente() {
        when(zonas.buscar(5L)).thenReturn(Optional.of(new Zone()));
        when(zonas.buscar(9L)).thenReturn(Optional.empty());

        assertEquals(10L, service.crear(5L, "insegura", null, 1L).getUserProfileId());
        assertThrows(ResourceNotFoundException.class, () -> service.crear(9L, "insegura", null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> service.crear(null, "insegura", null, 1L));
    }

    @Test
    void soloLaAutoraVeOBorraSuReporte() {
        ZoneReport propio = ZoneReport.nuevo(10L, 5L, "insegura", null);
        when(repositorio.buscar(3L)).thenReturn(Optional.of(propio));

        assertTrue(service.uno(3L, 1L).isPresent());
        assertThrows(AccessDeniedException.class, () -> service.eliminar(3L, 2L));
        service.eliminar(3L, 1L);
        verify(repositorio).eliminar(3L);
    }

    @Test
    void losReportesDeOtraUsuariaSoloLosVeLaAdministracion() {
        when(repositorio.dePerfil(10L)).thenReturn(List.of());
        when(repositorio.dePerfil(30L)).thenReturn(List.of());
        when(identidad.perfilDe(30L)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.deUsuaria(1L, 1L, false));
        assertEquals(List.of(), service.deUsuaria(10L, 1L, false));
        assertThrows(AccessDeniedException.class, () -> service.deUsuaria(30L, 1L, false));
        assertEquals(List.of(), service.deUsuaria(30L, 1L, true));
    }

    @Test
    void aprobarORechazarUnReporteInexistenteFalla() {
        when(repositorio.buscar(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.aprobar(99L, 1));
        assertThrows(ResourceNotFoundException.class, () -> service.rechazar(99L, 1));
    }
}
