package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.admin.application.port.out.*;
import com.alertamujer.admin.domain.model.AlertReport;
import com.alertamujer.admin.domain.model.CambiosInforme;
import com.alertamujer.alerts.api.AdminAlertView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InformesServiceTest {

    private final InformeRepositorio repository = mock(InformeRepositorio.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final UsuariasPort usuarias = mock(UsuariasPort.class);
    private final ZonasPort zonas = mock(ZonasPort.class);
    private final ExportadorInformesPort exportador = mock(ExportadorInformesPort.class);
    private final InformesService service = new InformesService(repository, alerts, usuarias, zonas, exportador);

    private static AdminAlertView alert(Long id, String status) {
        return new AdminAlertView(id, 10L, 1L, "Ana", "ana@correo.com", 1L, "main", "panic_button", status, null, null, null,
                LocalDateTime.of(2026, 10, 15, 8, 30), null, null, null);
    }

    private AlertReport informe;

    @BeforeEach
    void setUp() {
        informe = AlertReport.nuevo(new CambiosInforme("Informe", null, null, null, null, null, null, null, null, null, "resolved"), 2L);
        informe.setId(5L);
        when(repository.find(5L)).thenReturn(Optional.of(informe));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alerts.todas()).thenReturn(List.of(alert(1L, "resolved"), alert(2L, "active"), alert(3L, "resolved")));
        when(usuarias.find(anyLong())).thenReturn(Optional.empty());
        when(usuarias.porId()).thenReturn(Map.of());
    }

    @Test
    void elInformeCuentaSoloLasAlertasQueCumplenSusFiltros() {
        AlertReportView v = service.obtener(5L);

        assertEquals(2, v.alertCount());
        assertEquals(List.of(), v.alertIds());
    }

    @Test
    void exportarEnviaLasAlertasIncluidasYSumaUnaExportacion() {
        when(exportador.pdf(any(), any())).thenReturn(new byte[]{1, 2});

        assertArrayEquals(new byte[]{1, 2}, service.exportar(5L, true));

        verify(exportador).pdf(any(), argThat(l -> l.stream().map(AdminAlertView::id).toList().equals(List.of(1L, 3L))));
        verify(exportador, never()).csv(any(), any());
        assertEquals(1, informe.getExportCount());
        verify(repository).save(informe);
    }

    @Test
    void lasEstadisticasSumanExportacionesYZonasActivas() {
        informe.setExportCount(3);
        when(repository.todos()).thenReturn(List.of(informe));
        when(zonas.activas()).thenReturn(4L);

        var stats = service.stats();

        assertEquals(1, stats.totalReports());
        assertEquals(3, stats.totalAlerts());
        assertEquals(4, stats.activeZones());
        assertEquals(3, stats.exports());
    }
}
