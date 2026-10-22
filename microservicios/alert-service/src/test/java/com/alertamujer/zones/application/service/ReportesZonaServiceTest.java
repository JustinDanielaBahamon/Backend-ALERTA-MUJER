package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.out.IdentityPort;
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

    private final ReporteZonaRepositorio repository = mock(ReporteZonaRepositorio.class);
    private final ZonaRepositorio zonas = mock(ZonaRepositorio.class);
    private final IdentityPort identidad = mock(IdentityPort.class);
    private final ReportesZonaService service = new ReportesZonaService(repository, zonas, identidad);

    @BeforeEach
    void setUp() {
        when(identidad.perfilRequerido(1L)).thenReturn(10L);
        when(identidad.profileOf(1L)).thenReturn(Optional.of(10L));
        when(identidad.perfilRequerido(2L)).thenReturn(20L);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crearUsaElPerfilDelTokenYExigeUnaZonaExistente() {
        when(zonas.find(5L)).thenReturn(Optional.of(new Zone()));
        when(zonas.find(9L)).thenReturn(Optional.empty());

        assertEquals(10L, service.create(5L, "insegura", null, 1L).getUserProfileId());
        assertThrows(ResourceNotFoundException.class, () -> service.create(9L, "insegura", null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> service.create(null, "insegura", null, 1L));
    }

    @Test
    void soloLaAutoraVeOBorraSuReporte() {
        ZoneReport propio = ZoneReport.nuevo(10L, 5L, "insegura", null);
        when(repository.find(3L)).thenReturn(Optional.of(propio));

        assertTrue(service.uno(3L, 1L).isPresent());
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 2L));
        service.delete(3L, 1L);
        verify(repository).delete(3L);
    }

    @Test
    void losReportesDeOtraUsuariaSoloLosVeLaAdministracion() {
        when(repository.dePerfil(10L)).thenReturn(List.of());
        when(repository.dePerfil(30L)).thenReturn(List.of());
        when(identidad.profileOf(30L)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.deUsuaria(1L, 1L, false));
        assertEquals(List.of(), service.deUsuaria(10L, 1L, false));
        assertThrows(AccessDeniedException.class, () -> service.deUsuaria(30L, 1L, false));
        assertEquals(List.of(), service.deUsuaria(30L, 1L, true));
    }

    @Test
    void aprobarORechazarUnReporteInexistenteFalla() {
        when(repository.find(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.aprobar(99L, 1));
        assertThrows(ResourceNotFoundException.class, () -> service.rechazar(99L, 1));
    }
}
