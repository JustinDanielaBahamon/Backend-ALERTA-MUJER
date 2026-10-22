package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.AdministrarAlertasUseCase.DetalleAdmin;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase.CanalEnvio;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase.ContactoNotificado;
import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import com.alertamujer.alerts.domain.model.LocationLog;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
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

    @Mock private AlertaRepositorio repository;
    @Mock private IdentityPort identidad;
    @Mock private EventosAlertaPort events;
    @Mock private UbicacionRepositorio ubicaciones;
    @Mock private DestinatariosAlertaUseCase destinatarios;

    @InjectMocks private AdministracionAlertasService service;

    private static Alert alert(Long id, Long perfil, LocalDateTime inicio) {
        Alert a = new Alert();
        a.setId(id);
        a.setUserProfileId(perfil);
        a.setStartedAt(inicio);
        return a;
    }

    @Test
    void listar_agregaLaUsuariaYOrdenaDeLaMasReciente() {
        LocalDateTime ahora = LocalDateTime.now();
        when(repository.todas()).thenReturn(List.of(alert(1L, 10L, ahora.minusDays(1)), alert(2L, 20L, ahora)));
        when(identidad.usersByProfile()).thenReturn(Map.of(10L, new UserSummary(1L, "Ana", "Gómez", "ana@test.com", null, 1L, null)));

        List<AdminAlertView> vistas = service.listAll();

        assertEquals(2L, vistas.get(0).id());
        assertNull(vistas.get(0).userName());
        assertEquals("Ana Gómez", vistas.get(1).userName());
        assertEquals("ana@test.com", vistas.get(1).userEmail());
        assertEquals(1L, vistas.get(1).userId());
    }

    @Test
    void cambiarEstado_alTerminarUnaAlertaActivaAvisaALosContactos() {
        Alert activa = alert(1L, 10L, LocalDateTime.now());
        when(repository.find(1L)).thenReturn(Optional.of(activa));
        when(repository.save(activa)).thenReturn(activa);

        Alert resultado = service.cambiarEstado(1L, "resolved");

        assertEquals("resolved", resultado.getStatus());
        assertNotNull(resultado.getEndedAt());
        verify(events).alertaTerminada(eq(activa), any(LocalDateTime.class));
    }

    @Test
    void cambiarEstado_rechazaEstadosDesconocidosYAlertasInexistentes() {
        when(repository.find(1L)).thenReturn(Optional.of(alert(1L, 10L, LocalDateTime.now())));
        when(repository.find(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(1L, "borrada"));
        assertThrows(ResourceNotFoundException.class, () -> service.cambiarEstado(9L, "resolved"));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    private static LocationLog punto(int minuto, double lat) {
        LocationLog l = new LocationLog();
        l.setLatitude(java.math.BigDecimal.valueOf(lat));
        l.setLongitude(java.math.BigDecimal.valueOf(-75.28));
        l.setRecordedAt(LocalDateTime.of(2026, 10, 20, 10, minuto));
        return l;
    }

    @Test
    void detalle_daLaUltimaUbicacionYLosEnviosSinNombreNiCorreo() {
        Alert alert = new Alert();
        alert.setId(5L);
        alert.setStatus("active");
        when(repository.find(5L)).thenReturn(Optional.of(alert));
        when(ubicaciones.ofAlert(5L)).thenReturn(List.of(punto(1, 2.91), punto(7, 2.93), punto(3, 2.92)));
        when(destinatarios.contactosDe(alert)).thenReturn(List.of(new ContactoNotificado(9L, "Eva Ruiz", "Madre",
                List.of(new CanalEnvio("email", "eva@correo.com", "sent", LocalDateTime.of(2026, 10, 20, 10, 0), null)))));

        DetalleAdmin d = service.detalle(5L);

        assertTrue(d.ubicacionVisible());
        assertEquals(3, d.puntos());
        assertEquals(LocalDateTime.of(2026, 10, 20, 10, 7), d.ultimaUbicacion().recordedAt());
        assertEquals(1, d.envios().get(0).contacto());
        assertEquals("Madre", d.envios().get(0).relationship());
        assertEquals("sent", d.envios().get(0).channels().get(0).status());
        assertFalse(d.toString().contains("eva@correo.com"));
        assertFalse(d.toString().contains("Eva Ruiz"));
    }

    @Test
    void detalle_deUnaAlertaCanceladaNoMuestraUbicaciones() {
        Alert alert = new Alert();
        alert.setId(6L);
        alert.setStatus("cancelled");
        when(repository.find(6L)).thenReturn(Optional.of(alert));
        when(destinatarios.contactosDe(alert)).thenReturn(List.of());

        DetalleAdmin d = service.detalle(6L);

        assertFalse(d.ubicacionVisible());
        assertNull(d.ultimaUbicacion());
        verify(ubicaciones, never()).ofAlert(any());
    }
}
