package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.out.AlertasPort;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias.ArchivoEntrante;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias.ArchivoGuardado;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.application.port.out.IdentidadPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Casos de uso de la usuaria, probados contra puertos simulados (sin base ni almacenamiento reales). */
class EvidenciasServiceTest {

    private final EvidenciaRepositorio repositorio = mock(EvidenciaRepositorio.class);
    private final AlmacenamientoEvidencias almacenamiento = mock(AlmacenamientoEvidencias.class);
    private final AlertasPort alertas = mock(AlertasPort.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final EvidenciasService service = new EvidenciasService(repositorio, almacenamiento, alertas, identidad, 1000);

    private Evidence evidencia;

    @BeforeEach
    void preparar() {
        evidencia = new Evidence();
        evidencia.setId(4L);
        evidencia.setAlertId(9L);
        evidencia.setMediaType("video");
        evidencia.setFileName("video_1760870000000.mp4");
        when(repositorio.buscar(4L)).thenReturn(Optional.of(evidencia));
        when(repositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alertas.buscar(9L)).thenReturn(Optional.of(
                new AlertSummary(9L, 70L, "main", "panic_button", "active", null, null, null, null, null, null)));
        when(identidad.perfilDe(1L)).thenReturn(70L);
        when(identidad.perfilDe(2L)).thenReturn(71L);
    }

    private ArchivoEntrante archivo(String tipo, long tamano) {
        return new ArchivoEntrante("foto.jpg", tipo, tamano, () -> new ByteArrayInputStream(new byte[(int) tamano]));
    }

    @Test
    void renombrarGuardaElNombreNuevo() {
        assertEquals("Camino a casa.mp4", service.renombrar(4L, "Camino a casa", 1L).evidencia().getFileName());
    }

    @Test
    void otraUsuariaNoPuedeVerNiRenombrar() {
        assertThrows(AccessDeniedException.class, () -> service.una(4L, 2L));
        assertThrows(AccessDeniedException.class, () -> service.renombrar(4L, "x", 2L));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void subirGuardaElArchivoYSuHuella() {
        when(almacenamiento.guardar(any())).thenReturn(new ArchivoGuardado("clave", "abc123", 10));

        Evidence guardada = service.subir(archivo("image/jpeg", 10), 9L, 1L).evidencia();

        assertEquals("photo", guardada.getMediaType());
        assertEquals("abc123", guardada.getSha256());
        assertTrue(guardada.tieneArchivo());
    }

    @Test
    void siFallaElRegistroSeBorraElArchivoSubido() {
        when(almacenamiento.guardar(any())).thenReturn(new ArchivoGuardado("clave", "abc123", 10));
        when(repositorio.guardar(any())).thenThrow(new IllegalStateException("base caída"));

        assertThrows(IllegalStateException.class, () -> service.subir(archivo("image/jpeg", 10), 9L, 1L));
        verify(almacenamiento).eliminar("clave");
    }

    @Test
    void rechazaArchivosMuyGrandesAntesDeGuardarlos() {
        assertThrows(IllegalArgumentException.class, () -> service.subir(archivo("video/mp4", 5000), 9L, 1L));
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void listaLasEvidenciasDeLasAlertasDeLaUsuaria() {
        when(alertas.idsDeLaUsuaria(70L)).thenReturn(List.of(9L));
        when(repositorio.deLasAlertas(List.of(9L))).thenReturn(List.of(evidencia));
        when(alertas.buscarVarias(any())).thenReturn(Map.of());

        assertEquals(1, service.deLaUsuaria(1L).size());
    }
}
