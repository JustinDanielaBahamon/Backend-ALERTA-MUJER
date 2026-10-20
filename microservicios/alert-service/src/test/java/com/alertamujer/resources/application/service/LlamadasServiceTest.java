package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.out.IdentidadPort;
import com.alertamujer.resources.application.port.out.LlamadaRepositorio;
import com.alertamujer.resources.application.port.out.RecursoRepositorio;
import com.alertamujer.resources.domain.model.EmergencyResource;
import com.alertamujer.resources.domain.model.ResourceCall;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LlamadasServiceTest {

    private final LlamadaRepositorio repositorio = mock(LlamadaRepositorio.class);
    private final RecursoRepositorio recursos = mock(RecursoRepositorio.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final LlamadasService service = new LlamadasService(repositorio, recursos, identidad);

    @BeforeEach
    void setUp() {
        when(identidad.perfilRequerido(1L)).thenReturn(10L);
        when(identidad.perfilRequerido(2L)).thenReturn(20L);
        when(repositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registrarExigeUnRecursoExistenteYUsaElPerfilDelToken() {
        EmergencyResource r = new EmergencyResource();
        r.setId(7L);
        r.setTelephone("155");
        when(recursos.buscar(7L)).thenReturn(Optional.of(r));
        when(recursos.buscar(9L)).thenReturn(Optional.empty());

        assertEquals(10L, service.registrar(7L, null, null, null, null, 1L).getUserProfileId());
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null, null, null, null, null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> service.registrar(9L, null, null, null, null, 1L));
    }

    @Test
    void soloLaDuenaVeCambiaOBorraSusLlamadas() {
        ResourceCall c = new ResourceCall();
        c.setId(3L);
        c.setUserProfileId(10L);
        when(repositorio.buscar(3L)).thenReturn(Optional.of(c));

        assertTrue(service.una(3L, 1L).isPresent());
        assertTrue(service.una(3L, 2L).isEmpty());
        assertThrows(AccessDeniedException.class, () -> service.actualizar(3L, "failed", null, null, 2L));
        assertThrows(AccessDeniedException.class, () -> service.eliminar(3L, 2L));
        assertEquals("completed", service.actualizar(3L, "completed", LocalDateTime.now(), 30, 1L).getStatus());
        verify(repositorio, never()).eliminar(any());
    }
}
