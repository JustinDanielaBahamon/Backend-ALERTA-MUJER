package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.AdministrarEvidenciasUseCase.Estadisticas;
import com.alertamujer.evidence.application.port.in.AdministrarEvidenciasUseCase.EvidenciaAdmin;
import com.alertamujer.evidence.application.port.out.AlertasPort;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.application.port.out.IdentidadPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdministracionEvidenciasServiceTest {

    private final EvidenciaRepositorio repositorio = mock(EvidenciaRepositorio.class);
    private final AlertasPort alertas = mock(AlertasPort.class);
    private final IdentidadPort identidad = mock(IdentidadPort.class);
    private final AdministracionEvidenciasService service = new AdministracionEvidenciasService(repositorio, alertas, identidad);

    private Evidence evidencia(Long id, String revision, long tamanio) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(5L);
        e.setSizeBytes(tamanio);
        e.setStorageKey("clave-" + id);
        e.setReviewStatus(revision);
        return e;
    }

    @Test
    void listarAgregaUsuariaYAlerta() {
        AlertSummary alerta = new AlertSummary(5L, 10L, "main", "panic_button", "active", null, null, null, null, null, null);
        UserSummary u = new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
        when(repositorio.todas()).thenReturn(List.of(evidencia(1L, "pending", 100)));
        when(alertas.todas()).thenReturn(Map.of(5L, alerta));
        when(identidad.usuariasPorPerfil()).thenReturn(Map.of(10L, u));

        EvidenciaAdmin vista = service.listar().get(0);

        assertEquals("Ana Gómez", vista.duena().fullName());
        assertEquals("active", vista.alerta().status());
    }

    @Test
    void estadisticasCuentaPorRevisionYSumaTamanios() {
        when(repositorio.todas()).thenReturn(List.of(
                evidencia(1L, "verified", 100), evidencia(2L, "rejected", 50), evidencia(3L, "pending", 25)));

        Estadisticas stats = service.estadisticas();

        assertEquals(3, stats.total());
        assertEquals(1, stats.verified());
        assertEquals(1, stats.pending());
        assertEquals(1, stats.rejected());
        assertEquals(175, stats.totalSizeBytes());
    }

    @Test
    void cambiarRevisionGuardaElEstado() {
        when(repositorio.buscar(1L)).thenReturn(Optional.of(evidencia(1L, "pending", 100)));
        when(repositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alertas.buscar(5L)).thenReturn(Optional.empty());

        assertEquals("verified", service.cambiarRevision(1L, "verified").evidencia().getReviewStatus());
    }
}
