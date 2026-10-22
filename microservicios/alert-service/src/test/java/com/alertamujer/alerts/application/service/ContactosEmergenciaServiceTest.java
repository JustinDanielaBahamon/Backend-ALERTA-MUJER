package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.DatosContacto;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactosEmergenciaServiceTest {

    @Mock private ContactoRepositorio repository;
    @Mock private IdentityPort identidad;
    @Mock private AjustesPort ajustes;
    @Mock private CelularesPort celulares;

    @InjectMocks private ContactosEmergenciaService service;

    private static final DatosContacto EVA = new DatosContacto("Eva", "300", "eva@correo.com", "Hermana", null);

    @BeforeEach
    void setUp() {
        lenient().when(identidad.profileOf(1L)).thenReturn(Optional.of(10L));
    }

    @Test
    void crear_permiteHastaElLimiteYLaDuenaSaleDelToken() {
        when(ajustes.maximoContactos()).thenReturn(5);
        when(repository.ofUser(10L)).thenReturn(Collections.nCopies(4, new EmergencyContact()));
        when(repository.save(any(EmergencyContact.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyContact creado = service.create(EVA, 1L);

        assertEquals(10L, creado.getUserProfileId());
        assertEquals("Eva", creado.getContactName());
    }

    @Test
    void estadoApp_distingueConAppSinCelularYSinCuenta() {
        when(repository.ofUser(10L)).thenReturn(List.of(
                contacto(1L, "eva@correo.com"), contacto(2L, "ana@correo.com"), contacto(3L, "nadie@correo.com"), contacto(4L, null)));
        when(identidad.usuariaPorCorreo("eva@correo.com")).thenReturn(Optional.of(50L));
        when(identidad.usuariaPorCorreo("ana@correo.com")).thenReturn(Optional.of(60L));
        when(identidad.usuariaPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());
        when(celulares.tokensDeCelulares(50L)).thenReturn(List.of("token"));
        when(celulares.tokensDeCelulares(60L)).thenReturn(List.of());

        assertEquals(Map.of(1L, "app", 2L, "account", 3L, "none", 4L, "none"), service.estadoApp(1L));
    }

    private static EmergencyContact contacto(Long id, String correo) {
        EmergencyContact c = EmergencyContact.nuevo(10L, new DatosContacto("X", "300", correo, null, null));
        c.setId(id);
        return c;
    }

    @Test
    void crear_rechazaAlSuperarElLimite() {
        when(ajustes.maximoContactos()).thenReturn(5);
        when(repository.ofUser(10L)).thenReturn(Collections.nCopies(5, new EmergencyContact()));

        assertThrows(IllegalArgumentException.class, () -> service.create(EVA, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_rechazaUnaFotoQueNoEsImagen() {
        when(ajustes.maximoContactos()).thenReturn(5);
        when(repository.ofUser(10L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.create(new DatosContacto("Eva", null, null, null, "<script>"), 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void actualizarOEliminarUnContactoAjenoSeRechaza() {
        EmergencyContact ajeno = EmergencyContact.nuevo(20L, EVA);
        ajeno.setId(3L);
        when(repository.find(3L)).thenReturn(Optional.of(ajeno));

        assertThrows(AccessDeniedException.class, () -> service.actualizar(3L, EVA, 1L));
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 1L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void deUsuaria_losDeOtraUsuariaNoLosVeNadie() {
        // Ni siquiera la administración: son datos de terceros (auditoría S2)
        assertThrows(AccessDeniedException.class, () -> service.deUsuaria(5L, 1L));
        verify(repository, never()).ofUser(5L);
    }

    @Test
    void uno_soloDevuelveLosPropios() {
        EmergencyContact propio = EmergencyContact.nuevo(10L, EVA);
        EmergencyContact ajeno = EmergencyContact.nuevo(20L, EVA);
        when(repository.find(1L)).thenReturn(Optional.of(propio));
        when(repository.find(2L)).thenReturn(Optional.of(ajeno));

        assertTrue(service.uno(1L, 1L).isPresent());
        assertTrue(service.uno(2L, 1L).isEmpty());
    }
}
