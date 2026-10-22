package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.AuditoriaUseCase.AuditActor;
import com.alertamujer.admin.application.port.out.AuditRepository;
import com.alertamujer.admin.application.port.out.RequestContextPort;
import com.alertamujer.admin.application.port.out.UsersPort;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.AuditFilter;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    private final AuditRepository repository = mock(AuditRepository.class);
    private final UsersPort users = mock(UsersPort.class);
    private final AuditService service = new AuditService(repository, users);

    private static final UserSummary ADMIN = new UserSummary(2L, "Sara", "Ruiz", "sara@correo.com", null, UserSummary.ADMIN_ROLE, null);
    private static final UserSummary ANA = new UserSummary(5L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
    private static final AuditFilter TODO = new AuditFilter(null, null, null, null, null);

    @Test
    void thePageHasAMaxSizeAndAddsWhoMadeTheChange() {
        AuditLog r = AuditLog.sessionEvent(4L, "LOGIN_SUCCESS", null, null, null, null);
        when(repository.page(TODO, 0, 100)).thenReturn(new AuditRepository.AuditLogPage(List.of(r), 1, 0));
        when(users.byAccount()).thenReturn(Map.of(4L, ADMIN));

        var p = service.listAll(TODO, -3, 500);

        assertEquals(100, p.size());
        assertEquals("Sara Ruiz", p.items().get(0).actorName());
        assertEquals("Administración", p.items().get(0).actorRole());
    }

    @Test
    void theCsvNeutralizesFormulasAndMarksChangesWithoutActor() {
        AuditLog system = new AuditLog();
        system.setId(1L);
        system.setAction("UPDATE");
        system.setDescription("=HYPERLINK(\"x\")");
        when(repository.first(TODO, AuditService.MAX_EXPORT)).thenReturn(List.of(system));
        when(users.byAccount()).thenReturn(Map.of());

        String csv = new String(service.export(TODO), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("﻿ID;Fecha;Responsable;"));
        assertTrue(csv.contains("\"Sin responsable\""));
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"x\"\")\""));
    }

    @Test
    void theActorsAreOnlyAdminsSorted() {
        when(users.byAccount()).thenReturn(Map.of(4L, ADMIN, 9L, ANA));

        assertEquals(List.of(new AuditActor(4L, "Sara Ruiz", "sara@correo.com")), service.actors());
    }

    @Test
    void aSessionEventIsSavedWithTheRequestDataAndNeverFails() {
        RequestContextPort request = mock(RequestContextPort.class);
        when(request.device()).thenReturn("Chrome · Linux");
        when(request.ip()).thenReturn("1.2.3.4");
        SecurityEventsService events = new SecurityEventsService(repository, request);

        events.registrar(4L, "LOGIN_FAILED", "Contraseña incorrecta");
        ArgumentCaptor<AuditLog> r = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(r.capture());
        assertEquals("Contraseña incorrecta · Chrome · Linux", r.getValue().getDescription());
        assertEquals("1.2.3.4", r.getValue().getIpAddress());

        when(repository.save(any())).thenThrow(new IllegalStateException("base caída"));
        assertDoesNotThrow(() -> events.registrar(4L, "LOGOUT", null));
    }
}
