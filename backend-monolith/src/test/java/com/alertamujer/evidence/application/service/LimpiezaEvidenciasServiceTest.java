package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.application.port.out.AlertasPort;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.domain.model.Evidence;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LimpiezaEvidenciasServiceTest {

    private final EvidenciaRepositorio repositorio = mock(EvidenciaRepositorio.class);
    private final AlmacenamientoEvidencias almacenamiento = mock(AlmacenamientoEvidencias.class);
    private final AlertasPort alertas = mock(AlertasPort.class);
    private final LimpiezaEvidenciasService service = new LimpiezaEvidenciasService(repositorio, almacenamiento, alertas, 60);

    private Evidence evidencia(long id, long alertId) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(alertId);
        e.setStorageKey("clave-" + id);
        return e;
    }

    @Test
    void borraLasDeMasDe60DiasMenosLasDeAlertasConservadas() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 19, 4, 0);
        Evidence normal = evidencia(1, 10);
        Evidence conservada = evidencia(2, 20);
        when(repositorio.guardadasAntesDe(ahora.minusDays(60))).thenReturn(List.of(normal, conservada));
        when(alertas.conservanEvidencias(Set.of(10L, 20L))).thenReturn(Set.of(20L));

        assertEquals(1, service.limpiar(ahora));

        verify(repositorio).eliminarTodas(List.of(normal));
        verify(almacenamiento).eliminar("clave-1");
        verify(almacenamiento, never()).eliminar("clave-2");
    }

    @Test
    void sinEvidenciasViejasNoHaceNada() {
        when(repositorio.guardadasAntesDe(any())).thenReturn(List.of());

        assertEquals(0, service.limpiar(LocalDateTime.now()));
        verifyNoInteractions(almacenamiento, alertas);
    }
}
