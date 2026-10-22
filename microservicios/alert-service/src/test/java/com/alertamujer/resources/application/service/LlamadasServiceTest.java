package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.out.IdentityPort;
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

    private final LlamadaRepositorio repository = mock(LlamadaRepositorio.class);
    private final RecursoRepositorio recursos = mock(RecursoRepositorio.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final LlamadasService service = new LlamadasService(repository, recursos, identity);

    @BeforeEach
    void setUp() {
        when(identity.perfilRequerido(1L)).thenReturn(10L);
        when(identity.perfilRequerido(2L)).thenReturn(20L);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registrarExigeUnRecursoExistenteYUsaElPerfilDelToken() {
        EmergencyResource r = new EmergencyResource();
        r.setId(7L);
        r.setTelephone("155");
        when(recursos.find(7L)).thenReturn(Optional.of(r));
        when(recursos.find(9L)).thenReturn(Optional.empty());

        assertEquals(10L, service.registrar(7L, null, null, null, null, 1L).getUserProfileId());
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null, null, null, null, null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> service.registrar(9L, null, null, null, null, 1L));
    }

    @Test
    void soloLaDuenaVeCambiaOBorraSusLlamadas() {
        ResourceCall c = new ResourceCall();
        c.setId(3L);
        c.setUserProfileId(10L);
        when(repository.find(3L)).thenReturn(Optional.of(c));

        assertTrue(service.una(3L, 1L).isPresent());
        assertTrue(service.una(3L, 2L).isEmpty());
        assertThrows(AccessDeniedException.class, () -> service.update(3L, "failed", null, null, 2L));
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 2L));
        assertEquals("completed", service.update(3L, "completed", LocalDateTime.now(), 30, 1L).getStatus());
        verify(repository, never()).delete(any());
    }
}
