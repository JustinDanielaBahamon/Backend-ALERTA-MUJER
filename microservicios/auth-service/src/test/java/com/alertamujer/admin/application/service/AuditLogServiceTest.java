package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private IdentityApi identityApi;

    @InjectMocks
    private AuditLogService service;

    private final AuditLogService.AuditFilter sinFiltro = new AuditLogService.AuditFilter(null, null, null, null, null);

    private AuditLog registro(Long id, Long cuenta, String descripcion) {
        AuditLog r = new AuditLog();
        r.setId(id);
        r.setAccountId(cuenta);
        r.setAction("LOGIN_SUCCESS");
        r.setEntityType("SESSION");
        r.setDescription(descripcion);
        return r;
    }

    private void conAdmin() {
        when(identityApi.usersByAccountId()).thenReturn(java.util.Map.of(39L,
                new UserSummary(7L, "Admin", "Auditoria", "admin@test.local", null, 2L, null)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void listar_paginaYAgregaElResponsable() {
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(registro(2L, 39L, "Chrome"), registro(1L, null, null)),
                        inv.getArgument(1), 245));
        conAdmin();

        AuditLogService.AuditPage pagina = service.listar(sinFiltro, 1, 9999);

        assertEquals(245, pagina.total());
        assertEquals(AuditLogService.TAMANO_MAXIMO, pagina.size());
        assertEquals("Admin Auditoria", pagina.items().get(0).actorName());
        assertNull(pagina.items().get(1).actorName());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(any(Specification.class), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
    }

    @SuppressWarnings("unchecked")
    @Test
    void exportar_generaCsvParaExcel() {
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(registro(1L, 39L, "=HYPERLINK()")), PageRequest.of(0, 10), 1));
        conAdmin();

        String csv = new String(service.exportar(sinFiltro), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("﻿ID;Fecha;Responsable"));
        assertTrue(csv.contains("\"Admin Auditoria\""));
        assertTrue(csv.contains("\"'=HYPERLINK()\"")); // neutraliza fórmulas
    }
}
