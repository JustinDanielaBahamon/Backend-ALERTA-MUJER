package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactosEmergenciaServiceTest {

    @Mock private ContactoRepositorio repositorio;
    @Mock private IdentidadPort identidad;
    @Mock private AjustesPort ajustes;

    @InjectMocks private ContactosEmergenciaService service;

    private static final DatosContacto EVA = new DatosContacto("Eva", "300", "eva@correo.com", "Hermana", null);

    @BeforeEach
    void setUp() {
        lenient().when(identidad.perfilDe(1L)).thenReturn(Optional.of(10L));
    }

    @Test
    void crear_permiteHastaElLimiteYLaDuenaSaleDelToken() {
        when(ajustes.maximoContactos()).thenReturn(5);
        when(repositorio.deLaUsuaria(10L)).thenReturn(Collections.nCopies(4, new EmergencyContact()));
        when(repositorio.guardar(any(EmergencyContact.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyContact creado = service.crear(EVA, 1L);

        assertEquals(10L, creado.getUserProfileId());
        assertEquals("Eva", creado.getContactName());
    }

    @Test
    void crear_rechazaAlSuperarElLimite() {
        when(ajustes.maximoContactos()).thenReturn(5);
        when(repositorio.deLaUsuaria(10L)).thenReturn(Collections.nCopies(5, new EmergencyContact()));

        assertThrows(IllegalArgumentException.class, () -> service.crear(EVA, 1L));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void crear_rechazaUnaFotoQueNoEsImagen() {
        when(ajustes.maximoContactos()).thenReturn(5);
        when(repositorio.deLaUsuaria(10L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.crear(new DatosContacto("Eva", null, null, null, "<script>"), 1L));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void actualizarOEliminarUnContactoAjenoSeRechaza() {
        EmergencyContact ajeno = EmergencyContact.nuevo(20L, EVA);
        ajeno.setId(3L);
        when(repositorio.buscar(3L)).thenReturn(Optional.of(ajeno));

        assertThrows(AccessDeniedException.class, () -> service.actualizar(3L, EVA, 1L));
        assertThrows(AccessDeniedException.class, () -> service.eliminar(3L, 1L));
        verify(repositorio, never()).guardar(any());
        verify(repositorio, never()).eliminar(any());
    }

    @Test
    void deUsuaria_losDeOtraUsuariaNoLosVeNadie() {
        // Ni siquiera la administración: son datos de terceros (auditoría S2)
        assertThrows(AccessDeniedException.class, () -> service.deUsuaria(5L, 1L));
        verify(repositorio, never()).deLaUsuaria(5L);
    }

    @Test
    void uno_soloDevuelveLosPropios() {
        EmergencyContact propio = EmergencyContact.nuevo(10L, EVA);
        EmergencyContact ajeno = EmergencyContact.nuevo(20L, EVA);
        when(repositorio.buscar(1L)).thenReturn(Optional.of(propio));
        when(repositorio.buscar(2L)).thenReturn(Optional.of(ajeno));

        assertTrue(service.uno(1L, 1L).isPresent());
        assertTrue(service.uno(2L, 1L).isEmpty());
    }
}
