package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministracionAlertasServiceTest {

    @Mock private AlertaRepositorio repositorio;
    @Mock private IdentidadPort identidad;
    @Mock private EventosAlertaPort eventos;

    @InjectMocks private AdministracionAlertasService service;

    private static Alert alerta(Long id, Long perfil, LocalDateTime inicio) {
        Alert a = new Alert();
        a.setId(id);
        a.setUserProfileId(perfil);
        a.setStartedAt(inicio);
        return a;
    }

    @Test
    void listar_agregaLaUsuariaYOrdenaDeLaMasReciente() {
        LocalDateTime ahora = LocalDateTime.now();
        when(repositorio.todas()).thenReturn(List.of(alerta(1L, 10L, ahora.minusDays(1)), alerta(2L, 20L, ahora)));
        when(identidad.usuariasPorPerfil()).thenReturn(Map.of(10L, new UserSummary(1L, "Ana", "Gómez", "ana@test.com", null, 1L, null)));

        List<AdminAlertView> vistas = service.listar();

        assertEquals(2L, vistas.get(0).id());
        assertNull(vistas.get(0).userName());
        assertEquals("Ana Gómez", vistas.get(1).userName());
        assertEquals("ana@test.com", vistas.get(1).userEmail());
        assertEquals(1L, vistas.get(1).userId());
    }

    @Test
    void cambiarEstado_alTerminarUnaAlertaActivaAvisaALosContactos() {
        Alert activa = alerta(1L, 10L, LocalDateTime.now());
        when(repositorio.buscar(1L)).thenReturn(Optional.of(activa));
        when(repositorio.guardar(activa)).thenReturn(activa);

        Alert resultado = service.cambiarEstado(1L, "resolved");

        assertEquals("resolved", resultado.getStatus());
        assertNotNull(resultado.getEndedAt());
        verify(eventos).alertaTerminada(eq(activa), any(LocalDateTime.class));
    }

    @Test
    void cambiarEstado_rechazaEstadosDesconocidosYAlertasInexistentes() {
        when(repositorio.buscar(1L)).thenReturn(Optional.of(alerta(1L, 10L, LocalDateTime.now())));
        when(repositorio.buscar(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(1L, "borrada"));
        assertThrows(ResourceNotFoundException.class, () -> service.cambiarEstado(9L, "resolved"));
        verify(repositorio, never()).guardar(any());
        verifyNoInteractions(eventos);
    }
}
