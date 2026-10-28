package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.out.IdentityPort;
import com.alertamujer.resources.application.port.out.CallRepository;
import com.alertamujer.resources.application.port.out.ResourceRepository;
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

class CallsServiceTest {

    private final CallRepository repository = mock(CallRepository.class);
    private final ResourceRepository resources = mock(ResourceRepository.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final CallsService service = new CallsService(repository, resources, identity);

    @BeforeEach
    void setUp() {
        when(identity.requiredProfile(1L)).thenReturn(10L);
        when(identity.requiredProfile(2L)).thenReturn(20L);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void recordRequiresAnExistingResourceAndUsesTheTokenProfile() {
        EmergencyResource r = new EmergencyResource();
        r.setId(7L);
        r.setTelephone("155");
        when(resources.find(7L)).thenReturn(Optional.of(r));
        when(resources.find(9L)).thenReturn(Optional.empty());

        assertEquals(10L, service.registrar(7L, null, null, null, null, 1L).getUserProfileId());
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null, null, null, null, null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> service.registrar(9L, null, null, null, null, 1L));
    }

    @Test
    void onlyTheOwnerSeesChangesOrDeletesHerCalls() {
        ResourceCall c = new ResourceCall();
        c.setId(3L);
        c.setUserProfileId(10L);
        when(repository.find(3L)).thenReturn(Optional.of(c));

        assertTrue(service.one(3L, 1L).isPresent());
        assertTrue(service.one(3L, 2L).isEmpty());
        assertThrows(AccessDeniedException.class, () -> service.update(3L, "failed", null, null, 2L));
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 2L));
        assertEquals("completed", service.update(3L, "completed", LocalDateTime.now(), 30, 1L).getStatus());
        verify(repository, never()).delete(any());
    }
}
