package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.AuditoriaUseCase.AuditActor;
import com.alertamujer.admin.application.port.out.AuditoriaRepositorio;
import com.alertamujer.admin.application.port.out.ContextoPeticionPort;
import com.alertamujer.admin.application.port.out.UsuariasPort;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.FiltroAuditoria;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuditoriaServiceTest {

    private final AuditoriaRepositorio repository = mock(AuditoriaRepositorio.class);
    private final UsuariasPort usuarias = mock(UsuariasPort.class);
    private final AuditoriaService service = new AuditoriaService(repository, usuarias);

    private static final UserSummary ADMIN = new UserSummary(2L, "Sara", "Ruiz", "sara@correo.com", null, UserSummary.ROL_ADMIN, null);
    private static final UserSummary ANA = new UserSummary(5L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
    private static final FiltroAuditoria TODO = new FiltroAuditoria(null, null, null, null, null);

    @Test
    void laPaginaTieneUnTamanoMaximoYAgregaQuienHizoElCambio() {
        AuditLog r = AuditLog.eventoDeSesion(4L, "LOGIN_SUCCESS", null, null, null, null);
        when(repository.pagina(TODO, 0, 100)).thenReturn(new AuditoriaRepositorio.Pagina(List.of(r), 1, 0));
        when(usuarias.porCuenta()).thenReturn(Map.of(4L, ADMIN));

        var p = service.listAll(TODO, -3, 500);

        assertEquals(100, p.size());
        assertEquals("Sara Ruiz", p.items().get(0).actorName());
        assertEquals("Administración", p.items().get(0).actorRole());
    }

    @Test
    void elCsvNeutralizaFormulasYMarcaLosCambiosSinResponsable() {
        AuditLog sistema = new AuditLog();
        sistema.setId(1L);
        sistema.setAction("UPDATE");
        sistema.setDescription("=HYPERLINK(\"x\")");
        when(repository.primeros(TODO, AuditoriaService.MAXIMO_EXPORTACION)).thenReturn(List.of(sistema));
        when(usuarias.porCuenta()).thenReturn(Map.of());

        String csv = new String(service.exportar(TODO), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("﻿ID;Fecha;Responsable;"));
        assertTrue(csv.contains("\"Sin responsable\""));
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"x\"\")\""));
    }

    @Test
    void losResponsablesSonSoloAdministradorasOrdenadas() {
        when(usuarias.porCuenta()).thenReturn(Map.of(4L, ADMIN, 9L, ANA));

        assertEquals(List.of(new AuditActor(4L, "Sara Ruiz", "sara@correo.com")), service.responsables());
    }

    @Test
    void unEventoDeSesionSeGuardaConLosDatosDeLaPeticionYNuncaFalla() {
        ContextoPeticionPort peticion = mock(ContextoPeticionPort.class);
        when(peticion.dispositivo()).thenReturn("Chrome · Linux");
        when(peticion.ip()).thenReturn("1.2.3.4");
        EventosSeguridadService events = new EventosSeguridadService(repository, peticion);

        events.registrar(4L, "LOGIN_FAILED", "Contraseña incorrecta");
        ArgumentCaptor<AuditLog> r = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(r.capture());
        assertEquals("Contraseña incorrecta · Chrome · Linux", r.getValue().getDescription());
        assertEquals("1.2.3.4", r.getValue().getIpAddress());

        when(repository.save(any())).thenThrow(new IllegalStateException("base caída"));
        assertDoesNotThrow(() -> events.registrar(4L, "LOGOUT", null));
    }
}
