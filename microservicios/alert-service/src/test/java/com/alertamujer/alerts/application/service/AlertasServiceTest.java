package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevaAlerta;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.CambiosAlerta;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Casos de uso de las alertas con los puertos simulados (sin base de datos ni otros servicios). */
@ExtendWith(MockitoExtension.class)
class AlertasServiceTest {

    @Mock private AlertaRepositorio repository;
    @Mock private DestinatariosService destinatarios;
    @Mock private IdentityPort identity;
    @Mock private EventosAlertaPort events;

    @InjectMocks private AlertasService service;

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
        alert.setId(1L);
        alert.setUserProfileId(10L);
        alert.setDeviceId(1L);
        alert.setActivationMethod("panic_button");
        alert.setStartedAt(LocalDateTime.now());
    }

    private void profile(Long userId, Long profile) {
        when(identity.profileOf(userId)).thenReturn(Optional.of(profile));
    }

    private static NuevaAlerta newOne(List<Long> contactIds) {
        return new NuevaAlerta(1L, null, "panic_button", null, null, BigDecimal.valueOf(4.6), BigDecimal.valueOf(-74.1),
                null, contactIds);
    }

    @Test
    void activar_laDuenaSaleDelTokenYSeAvisaALosContactos() {
        profile(1L, 10L);
        when(repository.enCurso(10L)).thenReturn(Optional.empty());
        when(repository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(7L);
            return a;
        });

        Alert created = service.activar(newOne(List.of(3L)), 1L);

        assertEquals(10L, created.getUserProfileId());
        assertEquals("main", created.getAlertType());
        assertEquals("active", created.getStatus());
        assertEquals(BigDecimal.valueOf(4.6), created.getLatitude());
        verify(destinatarios).registrar(created, List.of(3L));
    }

    @Test
    void activar_conUnaAlertaEnCursoLaDevuelveSinAvisarOtraVez() {
        profile(1L, 10L);
        when(repository.enCurso(10L)).thenReturn(Optional.of(alert));

        assertSame(alert, service.activar(newOne(null), 1L));

        verify(repository, never()).save(any());
        verifyNoInteractions(destinatarios);
    }

    @Test
    void activar_sinPerfilFalla() {
        when(identity.profileOf(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.activar(newOne(null), 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void actualizar_alTerminarLaAlertaAvisaALosContactos() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));
        when(repository.save(alert)).thenReturn(alert);

        Alert resultado = service.update(1L, new CambiosAlerta(null, null, "resolved", null, null, null), 1L);

        assertEquals("resolved", resultado.getStatus());
        assertNotNull(resultado.getEndedAt());
        verify(events).alertaTerminada(eq(alert), any(LocalDateTime.class));
    }

    @Test
    void actualizar_sinCambioDeEstadoNoAvisa() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));
        when(repository.save(alert)).thenReturn(alert);

        service.update(1L, new CambiosAlerta(null, null, null, "Voy en taxi", null, null), 1L);

        verifyNoInteractions(events);
    }

    @Test
    void actualizarOEliminarUnaAlertaAjenaSeRechaza() {
        profile(2L, 20L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));

        assertThrows(AccessDeniedException.class,
                () -> service.update(1L, new CambiosAlerta(null, null, "cancelled", null, null, null), 2L));
        assertThrows(AccessDeniedException.class, () -> service.delete(1L, 2L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void eliminar_laDuenaBorraSuAlerta() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));

        service.delete(1L, 1L);

        verify(repository).delete(1L);
    }

    @Test
    void una_soloDevuelveLasPropias() {
        profile(1L, 10L);
        profile(2L, 20L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));

        assertTrue(service.una(1L, 1L).isPresent());
        assertTrue(service.una(1L, 2L).isEmpty());
    }

    @Test
    void deUsuaria_aceptaElIdDeUsuariaOElDePerfilPropio() {
        profile(1L, 10L);
        when(identity.user(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", null, "ana@test.com", null, 1L, null)));
        when(repository.ofUser(10L)).thenReturn(List.of(alert));

        assertEquals(1, service.ofUser(1L, 1L).size());
        assertEquals(1, service.ofUser(10L, 1L).size());
    }

    @Test
    void deUsuaria_otraUsuariaSoloLaVeLaAdministracion() {
        profile(1L, 10L);
        when(identity.user(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", null, "ana@test.com", null, 1L, null)));

        assertThrows(AccessDeniedException.class, () -> service.ofUser(5L, 1L));

        profile(2L, 30L);
        profile(5L, 50L);
        when(identity.user(2L)).thenReturn(Optional.of(new UserSummary(2L, "Admin", null, "admin@test.com", null, UserSummary.ADMIN_ROLE, null)));
        when(repository.ofUser(50L)).thenReturn(List.of(alert));

        assertEquals(List.of(alert), service.ofUser(5L, 2L));
    }

    @Test
    void conservarEvidencias_cuentaLasQueYaConserva() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));
        when(repository.kept(10L)).thenReturn((long) Alert.MAX_ALERTAS_CONSERVADAS);

        assertThrows(IllegalArgumentException.class, () -> service.conservarEvidencias(1L, true, 1L));

        when(repository.kept(10L)).thenReturn(4L);
        when(repository.save(alert)).thenReturn(alert);
        assertTrue(service.conservarEvidencias(1L, true, 1L).isKeepEvidence());
    }

    @Test
    void consultas_paraOtrosModulos() {
        Alert other = new Alert();
        other.setId(2L);
        other.setUserProfileId(10L);
        other.setKeepEvidence(true);
        Alert ajena = new Alert();
        ajena.setId(3L);
        ajena.setUserProfileId(20L);
        when(repository.all()).thenReturn(List.of(alert, other, ajena));
        when(repository.findMany(Set.of(1L, 2L))).thenReturn(List.of(alert, other));

        assertEquals(2L, service.countByProfile().get(10L));
        assertEquals(Set.of(2L), service.keepEvidence(Set.of(1L, 2L)));
        assertEquals(Set.of(), service.keepEvidence(Set.of()));
        assertTrue(service.find(null).isEmpty());
    }

    @Test
    void mias_deLaMasRecienteALaMasAntigua() {
        profile(1L, 10L);
        Alert vieja = new Alert();
        vieja.setStartedAt(LocalDateTime.now().minusDays(1));
        when(repository.ofUser(10L)).thenReturn(List.of(vieja, alert));

        assertEquals(List.of(alert, vieja), service.mias(1L));
    }

    @Test
    void activar_noConfiaEnLaFechaDeCreacionSiNoLlega() {
        profile(1L, 10L);
        when(repository.enCurso(10L)).thenReturn(Optional.empty());
        when(repository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activar(newOne(null), 1L);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(repository).save(captor.capture());
        assertNotNull(captor.getValue().getStartedAt());
        assertNull(captor.getValue().getId());
    }
}
