package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase.Bloqueo;
import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase.HistorialItem;
import com.alertamujer.admin.application.port.out.*;
import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import com.alertamujer.admin.domain.model.ModerationAction;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Moderación de cuentas con los puertos simulados. */
class ModeracionCuentasServiceTest {

    private final ReporteUsuariaRepositorio casos = mock(ReporteUsuariaRepositorio.class);
    private final AccionModeracionRepositorio acciones = mock(AccionModeracionRepositorio.class);
    private final SolicitudEliminacionRepositorio solicitudes = mock(SolicitudEliminacionRepositorio.class);
    private final CuentasPort cuentas = mock(CuentasPort.class);
    private final UsuariasPort usuarias = mock(UsuariasPort.class);
    private final ModeracionCuentasService service = new ModeracionCuentasService(casos, acciones, solicitudes, cuentas, usuarias);

    private static final UserSummary ANA = new UserSummary(5L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
    private static final UserSummary ADMIN = new UserSummary(2L, "Sara", "Ruiz", "sara@correo.com", null, UserSummary.ROL_ADMIN, null);

    @BeforeEach
    void setUp() {
        when(usuarias.find(5L)).thenReturn(Optional.of(ANA));
        when(usuarias.porId()).thenReturn(Map.of(5L, ANA, 2L, ADMIN));
        when(casos.save(any())).thenAnswer(inv -> {
            UserReport r = inv.getArgument(0);
            r.setId(40L);
            return r;
        });
        when(solicitudes.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void bloquearCambiaLaCuentaYDejaUnCasoConSuAccion() {
        service.bloquear(5L, new Bloqueo("Acoso", "Mensajes ofensivos reiterados", null), 2L);

        verify(cuentas).actualizar(5L, new UserAccountChanges("blocked", null, null, null, null), 2L);
        ArgumentCaptor<ModerationAction> accion = ArgumentCaptor.forClass(ModerationAction.class);
        verify(acciones).save(accion.capture());
        assertEquals(40L, accion.getValue().getUserReportId());
        assertEquals("block", accion.getValue().getActionType());
    }

    @Test
    void bloquearValidaAntesDeTocarLaCuenta() {
        assertThrows(IllegalArgumentException.class, () -> service.bloquear(5L, new Bloqueo(" ", "Mensajes ofensivos", null), 2L));
        assertThrows(IllegalArgumentException.class, () -> service.bloquear(5L, new Bloqueo("Acoso", "Corto", null), 2L));
        assertThrows(IllegalArgumentException.class, () -> service.bloquear(5L, new Bloqueo("Acoso", "Mensajes ofensivos", "<script>"), 2L));
        assertThrows(ResourceNotFoundException.class, () -> service.bloquear(9L, new Bloqueo("Acoso", "Mensajes ofensivos", null), 2L));
        verifyNoInteractions(cuentas);
    }

    @Test
    void desbloquearAnotaEnElUltimoCasoOCreaUno() {
        when(casos.deUsuaria(5L)).thenReturn(List.of());
        service.desbloquear(5L, "Se revisó el caso con ella", 2L);
        verify(casos).save(argThat(r -> "Desbloqueo".equals(r.getReason())));
        verify(cuentas).actualizar(5L, new UserAccountChanges("active", null, null, null, null), 2L);
    }

    @Test
    void elHistorialJuntaAccionesYSolicitudesDelMasReciente() {
        UserReport caso = UserReport.bloqueo(5L, 2L, "Acoso", "Mensajes", null, LocalDateTime.now());
        caso.setId(40L);
        ModerationAction bloqueo = ModerationAction.de(40L, 2L, "block", "Mensajes");
        bloqueo.setCreatedAt(LocalDateTime.now().minusDays(1));
        AccountDeletionRequest pedida = AccountDeletionRequest.nueva(5L, 2L, "Cuenta duplicada");
        when(casos.deUsuaria(5L)).thenReturn(List.of(caso));
        when(acciones.deCasos(any())).thenReturn(List.of(bloqueo));
        when(solicitudes.todas()).thenReturn(List.of(pedida));

        List<HistorialItem> h = service.historial(5L);

        assertEquals(List.of("deletion_pending", "block"), h.stream().map(HistorialItem::type).toList());
        assertEquals("Acoso", h.get(1).motivo());
        assertEquals("Sara Ruiz", h.get(1).adminNombre());
    }

    @Test
    void noSePideDosVecesNiSeApruebaPorLaMismaAdministradora() {
        when(solicitudes.hayPendiente(5L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.solicitarEliminacion(5L, "Cuenta duplicada", 2L));

        AccountDeletionRequest s = AccountDeletionRequest.nueva(5L, 2L, "Cuenta duplicada");
        when(solicitudes.find(7L)).thenReturn(Optional.of(s));
        assertThrows(IllegalArgumentException.class, () -> service.aprobar(7L, null, 2L));
        verify(cuentas, never()).anonimizar(any());

        assertEquals("approved", service.aprobar(7L, "Confirmado", 3L).status());
        verify(cuentas).anonimizar(5L);
    }
}
