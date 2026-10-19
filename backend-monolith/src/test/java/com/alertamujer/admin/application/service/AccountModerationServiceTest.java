package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.service.AccountModerationService.BloqueoRequest;
import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import com.alertamujer.admin.domain.model.ModerationAction;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.admin.infrastructure.repository.AccountDeletionRequestRepository;
import com.alertamujer.admin.infrastructure.repository.ModerationActionRepository;
import com.alertamujer.admin.infrastructure.repository.UserReportRepository;
import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AccountModerationServiceTest {

    private final UserReportRepository reportes = mock(UserReportRepository.class);
    private final ModerationActionRepository acciones = mock(ModerationActionRepository.class);
    private final AccountDeletionRequestRepository solicitudes = mock(AccountDeletionRequestRepository.class);
    private final IdentityAdminApi identityAdminApi = mock(IdentityAdminApi.class);
    private final IdentityApi identityApi = mock(IdentityApi.class);
    private final AccountModerationService service =
            new AccountModerationService(reportes, acciones, solicitudes, identityAdminApi, identityApi);

    @BeforeEach
    void preparar() {
        when(identityApi.findUser(5L)).thenReturn(Optional.of(mock(UserSummary.class)));
        when(identityApi.usersById()).thenReturn(Map.of());
        when(reportes.save(any())).thenAnswer(inv -> {
            UserReport r = inv.getArgument(0);
            r.setId(40L);
            return r;
        });
        when(solicitudes.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void bloquearExigeMotivoYQuedaComoCasoEnModeracion() {
        service.bloquear(5L, new BloqueoRequest("Falsas alarmas", "Activó 30 alertas falsas esta semana", null), 1L);

        verify(identityAdminApi).updateUserAccount(eq(5L), eq(new UserAccountChanges("blocked", null, null, null, null)), eq(1L));
        ArgumentCaptor<UserReport> caso = ArgumentCaptor.forClass(UserReport.class);
        verify(reportes).save(caso.capture());
        assertEquals("Falsas alarmas", caso.getValue().getReason());
        assertEquals(1L, caso.getValue().getReporterUserId());
        ArgumentCaptor<ModerationAction> accion = ArgumentCaptor.forClass(ModerationAction.class);
        verify(acciones).save(accion.capture());
        assertEquals("block", accion.getValue().getActionType());
        assertEquals(40L, accion.getValue().getUserReportId());
    }

    @Test
    void sinExplicacionNoSeBloquea() {
        assertThrows(IllegalArgumentException.class,
                () -> service.bloquear(5L, new BloqueoRequest("Fraude", "corto", null), 1L));
        assertThrows(IllegalArgumentException.class,
                () -> service.bloquear(5L, new BloqueoRequest(" ", "Una explicación suficientemente larga", null), 1L));
        verifyNoInteractions(identityAdminApi);
    }

    @Test
    void laEliminacionLaApruebaOtraAdministradora() {
        AccountDeletionRequest s = new AccountDeletionRequest();
        s.setId(3L);
        s.setUserId(5L);
        s.setRequestedBy(1L);
        s.setReason("Lo pidió la usuaria por correo");
        when(solicitudes.findById(3L)).thenReturn(Optional.of(s));

        assertThrows(IllegalArgumentException.class, () -> service.aprobar(3L, null, 1L));
        verify(identityAdminApi, never()).anonymizeAccount(any());

        assertEquals("approved", service.aprobar(3L, "Confirmado", 2L).status());
        verify(identityAdminApi).anonymizeAccount(5L);
    }

    @Test
    void noSeDuplicanSolicitudesPendientes() {
        when(solicitudes.existsByUserIdAndStatus(5L, "pending")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarEliminacion(5L, "Cuenta duplicada de otra usuaria", 1L));
    }
}
