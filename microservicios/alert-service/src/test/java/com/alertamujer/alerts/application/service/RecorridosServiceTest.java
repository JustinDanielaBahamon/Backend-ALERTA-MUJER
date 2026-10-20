package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevoRecorrido;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
import com.alertamujer.alerts.application.port.out.RecorridoRepositorio;
import com.alertamujer.alerts.domain.model.RoutePoint;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecorridosServiceTest {

    @Mock private RecorridoRepositorio repositorio;
    @Mock private IdentidadPort identidad;

    @InjectMocks private RecorridosService service;

    private static final List<RoutePoint> PUNTOS = List.of(new RoutePoint(4.6, -74.1, null), new RoutePoint(4.7, -74.0, null));

    @BeforeEach
    void setUp() {
        lenient().when(identidad.perfilRequerido(1L)).thenReturn(10L);
    }

    private static NuevoRecorrido nuevo() {
        return new NuevoRecorrido(" Casa ", "A", "B", "Bogotá", null, null, null, null, PUNTOS, null);
    }

    @Test
    void crear_esEstrellaMientrasHayaCupo() {
        when(repositorio.contar(10L)).thenReturn(1L);
        when(repositorio.contarEstrella(10L)).thenReturn(1L);
        when(repositorio.guardar(any(SavedRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        SavedRoute r = service.crear(nuevo(), 1L);

        assertEquals("Casa", r.getName());
        assertEquals(10L, r.getUserProfileId());
        assertTrue(r.getIsStarred());
        assertTrue(r.getIsManual());
    }

    @Test
    void crear_conLos4OcupadosSeRechaza() {
        when(repositorio.contar(10L)).thenReturn(4L);

        assertThrows(ConflictException.class, () -> service.crear(nuevo(), 1L));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void unRecorridoAjenoNoSePuedeTocar() {
        SavedRoute ajeno = new SavedRoute();
        ajeno.setId(3L);
        ajeno.setUserProfileId(20L);
        when(repositorio.buscar(3L)).thenReturn(Optional.of(ajeno));

        assertThrows(AccessDeniedException.class, () -> service.actualizar(3L, "Otro", null, 1L));
        assertThrows(AccessDeniedException.class, () -> service.eliminar(3L, 1L));
        verify(repositorio, never()).eliminar(any());
    }
}
