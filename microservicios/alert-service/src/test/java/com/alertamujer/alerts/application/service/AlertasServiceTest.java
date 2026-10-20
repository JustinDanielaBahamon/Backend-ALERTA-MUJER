package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevaAlerta;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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

    @Mock private AlertaRepositorio repositorio;
    @Mock private DestinatariosService destinatarios;
    @Mock private IdentidadPort identidad;
    @Mock private EventosAlertaPort eventos;

    @InjectMocks private AlertasService service;

    private Alert alerta;

    @BeforeEach
    void setUp() {
        alerta = new Alert();
        alerta.setId(1L);
        alerta.setUserProfileId(10L);
        alerta.setDeviceId(1L);
        alerta.setActivationMethod("panic_button");
        alerta.setStartedAt(LocalDateTime.now());
    }

    private void perfil(Long userId, Long perfil) {
        when(identidad.perfilDe(userId)).thenReturn(Optional.of(perfil));
    }

    private static NuevaAlerta nueva(List<Long> contactIds) {
        return new NuevaAlerta(1L, null, "panic_button", null, null, BigDecimal.valueOf(4.6), BigDecimal.valueOf(-74.1),
                null, contactIds);
    }

    @Test
    void activar_laDuenaSaleDelTokenYSeAvisaALosContactos() {
        perfil(1L, 10L);
        when(repositorio.enCurso(10L)).thenReturn(Optional.empty());
        when(repositorio.guardar(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(7L);
            return a;
        });

        Alert creada = service.activar(nueva(List.of(3L)), 1L);

        assertEquals(10L, creada.getUserProfileId());
        assertEquals("main", creada.getAlertType());
        assertEquals("active", creada.getStatus());
        assertEquals(BigDecimal.valueOf(4.6), creada.getLatitude());
        verify(destinatarios).registrar(creada, List.of(3L));
    }

    @Test
    void activar_conUnaAlertaEnCursoLaDevuelveSinAvisarOtraVez() {
        perfil(1L, 10L);
        when(repositorio.enCurso(10L)).thenReturn(Optional.of(alerta));

        assertSame(alerta, service.activar(nueva(null), 1L));

        verify(repositorio, never()).guardar(any());
        verifyNoInteractions(destinatarios);
    }

    @Test
    void activar_sinPerfilFalla() {
        when(identidad.perfilDe(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.activar(nueva(null), 1L));
        verifyNoInteractions(repositorio);
    }

    @Test
    void actualizar_alTerminarLaAlertaAvisaALosContactos() {
        perfil(1L, 10L);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta));
        when(repositorio.guardar(alerta)).thenReturn(alerta);

        Alert resultado = service.actualizar(1L, new CambiosAlerta(null, null, "resolved", null, null, null), 1L);

        assertEquals("resolved", resultado.getStatus());
        assertNotNull(resultado.getEndedAt());
        verify(eventos).alertaTerminada(eq(alerta), any(LocalDateTime.class));
    }

    @Test
    void actualizar_sinCambioDeEstadoNoAvisa() {
        perfil(1L, 10L);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta));
        when(repositorio.guardar(alerta)).thenReturn(alerta);

        service.actualizar(1L, new CambiosAlerta(null, null, null, "Voy en taxi", null, null), 1L);

        verifyNoInteractions(eventos);
    }

    @Test
    void actualizarOEliminarUnaAlertaAjenaSeRechaza() {
        perfil(2L, 20L);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta));

        assertThrows(AccessDeniedException.class,
                () -> service.actualizar(1L, new CambiosAlerta(null, null, "cancelled", null, null, null), 2L));
        assertThrows(AccessDeniedException.class, () -> service.eliminar(1L, 2L));
        verify(repositorio, never()).guardar(any());
        verify(repositorio, never()).eliminar(any());
    }

    @Test
    void eliminar_laDuenaBorraSuAlerta() {
        perfil(1L, 10L);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta));

        service.eliminar(1L, 1L);

        verify(repositorio).eliminar(1L);
    }

    @Test
    void una_soloDevuelveLasPropias() {
        perfil(1L, 10L);
        perfil(2L, 20L);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta));

        assertTrue(service.una(1L, 1L).isPresent());
        assertTrue(service.una(1L, 2L).isEmpty());
    }

    @Test
    void deUsuaria_aceptaElIdDeUsuariaOElDePerfilPropio() {
        perfil(1L, 10L);
        when(identidad.usuaria(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", null, "ana@test.com", null, 1L, null)));
        when(repositorio.deLaUsuaria(10L)).thenReturn(List.of(alerta));

        assertEquals(1, service.deUsuaria(1L, 1L).size());
        assertEquals(1, service.deUsuaria(10L, 1L).size());
    }

    @Test
    void deUsuaria_otraUsuariaSoloLaVeLaAdministracion() {
        perfil(1L, 10L);
        when(identidad.usuaria(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", null, "ana@test.com", null, 1L, null)));

        assertThrows(AccessDeniedException.class, () -> service.deUsuaria(5L, 1L));

        perfil(2L, 30L);
        perfil(5L, 50L);
        when(identidad.usuaria(2L)).thenReturn(Optional.of(new UserSummary(2L, "Admin", null, "admin@test.com", null, UserSummary.ROL_ADMIN, null)));
        when(repositorio.deLaUsuaria(50L)).thenReturn(List.of(alerta));

        assertEquals(List.of(alerta), service.deUsuaria(5L, 2L));
    }

    @Test
    void conservarEvidencias_cuentaLasQueYaConserva() {
        perfil(1L, 10L);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta));
        when(repositorio.conservadas(10L)).thenReturn((long) Alert.MAX_ALERTAS_CONSERVADAS);

        assertThrows(IllegalArgumentException.class, () -> service.conservarEvidencias(1L, true, 1L));

        when(repositorio.conservadas(10L)).thenReturn(4L);
        when(repositorio.guardar(alerta)).thenReturn(alerta);
        assertTrue(service.conservarEvidencias(1L, true, 1L).isKeepEvidence());
    }

    @Test
    void consultas_paraOtrosModulos() {
        Alert otra = new Alert();
        otra.setId(2L);
        otra.setUserProfileId(10L);
        otra.setKeepEvidence(true);
        Alert ajena = new Alert();
        ajena.setId(3L);
        ajena.setUserProfileId(20L);
        when(repositorio.todas()).thenReturn(List.of(alerta, otra, ajena));
        when(repositorio.buscarVarias(Set.of(1L, 2L))).thenReturn(List.of(alerta, otra));

        assertEquals(2L, service.conteoPorPerfil().get(10L));
        assertEquals(Set.of(2L), service.conservanEvidencias(Set.of(1L, 2L)));
        assertEquals(Set.of(), service.conservanEvidencias(Set.of()));
        assertTrue(service.buscar(null).isEmpty());
    }

    @Test
    void mias_deLaMasRecienteALaMasAntigua() {
        perfil(1L, 10L);
        Alert vieja = new Alert();
        vieja.setStartedAt(LocalDateTime.now().minusDays(1));
        when(repositorio.deLaUsuaria(10L)).thenReturn(List.of(vieja, alerta));

        assertEquals(List.of(alerta, vieja), service.mias(1L));
    }

    @Test
    void activar_noConfiaEnLaFechaDeCreacionSiNoLlega() {
        perfil(1L, 10L);
        when(repositorio.enCurso(10L)).thenReturn(Optional.empty());
        when(repositorio.guardar(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activar(nueva(null), 1L);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(repositorio).guardar(captor.capture());
        assertNotNull(captor.getValue().getStartedAt());
        assertNull(captor.getValue().getId());
    }
}
