package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.admin.infrastructure.repository.UserReportRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReportServiceTest {

    @Mock
    private UserReportRepository userReportRepository;

    @Mock
    private IdentityApi identityApi;

    @InjectMocks
    private UserReportService service;

    private UserSummary usuaria(Long id, String nombre, String correo) {
        return new UserSummary(id, nombre, null, correo, null, 1L, null);
    }

    @Test
    void crear_buscaLaUsuariaPorCorreoYQuedaPendiente() {
        when(identityApi.findUserByEmail("ana@correo.com")).thenReturn(Optional.of(usuaria(2L, "Ana", "ana@correo.com")));
        when(userReportRepository.save(any(UserReport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(identityApi.findUser(2L)).thenReturn(Optional.of(usuaria(2L, "Ana", "ana@correo.com")));
        when(identityApi.findUser(9L)).thenReturn(Optional.of(usuaria(9L, "Admin", "admin@correo.com")));

        UserReportService.UserReportView vista = service.crear(
                new UserReportService.UserReportRequest("ana@correo.com", "Acoso", "Mensajes ofensivos", "high", "resolved", null), 9L);

        assertEquals(2L, vista.reportedUserId());
        assertEquals("Ana", vista.reportedUserName());
        assertEquals(9L, vista.reporterUserId());
        assertEquals("high", vista.severity());
        assertEquals("pending", vista.status()); // un reporte nuevo nunca nace cerrado
    }

    @Test
    void crear_rechazaCorreosQueNoExisten() {
        when(identityApi.findUserByEmail("nadie@correo.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.crear(
                new UserReportService.UserReportRequest("nadie@correo.com", "Spam", "x", "low", null, null), 9L));
    }

    @Test
    void actualizar_alCerrarGuardaRevisoraYFecha() {
        UserReport r = new UserReport();
        r.setId(1L);
        r.setReportedUserId(2L);
        when(userReportRepository.findById(1L)).thenReturn(Optional.of(r));
        when(userReportRepository.save(any(UserReport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(identityApi.findUser(2L)).thenReturn(Optional.empty());

        UserReportService.UserReportView abierto = service.actualizar(1L,
                new UserReportService.UserReportRequest(null, null, null, null, "in_review", null), 9L);
        assertNull(abierto.reviewedAt());

        UserReportService.UserReportView cerrado = service.actualizar(1L,
                new UserReportService.UserReportRequest(null, null, null, "critical", "resolved", "Cuenta advertida"), 9L);
        assertEquals("resolved", cerrado.status());
        assertEquals("critical", cerrado.severity());
        assertEquals(9, cerrado.reviewedByAdminId());
        assertNotNull(cerrado.reviewedAt());
    }

    @Test
    void actualizar_rechazaEstadosYGravedadesDesconocidos() {
        UserReport r = new UserReport();
        r.setId(1L);
        when(userReportRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L,
                new UserReportService.UserReportRequest(null, null, null, null, "Resuelto", null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L,
                new UserReportService.UserReportRequest(null, null, null, "Alta", null, null), 9L));
    }
}
