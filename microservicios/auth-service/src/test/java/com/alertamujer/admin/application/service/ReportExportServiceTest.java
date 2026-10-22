package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.ReportExportUseCase.AlertReportView;
import com.alertamujer.admin.application.port.out.*;
import com.alertamujer.admin.domain.model.AlertReport;
import com.alertamujer.admin.domain.model.ReportExportChanges;
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

class ReportExportServiceTest {

    private final ReportExportRepository repository = mock(ReportExportRepository.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final UsersPort users = mock(UsersPort.class);
    private final ZonesPort zones = mock(ZonesPort.class);
    private final ReportExporterPort exporter = mock(ReportExporterPort.class);
    private final ReportExportService service = new ReportExportService(repository, alerts, users, zones, exporter);

    private static AdminAlertView alert(Long id, String status) {
        return new AdminAlertView(id, 10L, 1L, "Ana", "ana@correo.com", 1L, "main", "panic_button", status, null, null, null,
                LocalDateTime.of(2026, 10, 15, 8, 30), null, null, null);
    }

    private AlertReport report;

    @BeforeEach
    void setUp() {
        report = AlertReport.created(new ReportExportChanges("Informe", null, null, null, null, null, null, null, null, null, "resolved"), 2L);
        report.setId(5L);
        when(repository.find(5L)).thenReturn(Optional.of(report));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alerts.all()).thenReturn(List.of(alert(1L, "resolved"), alert(2L, "active"), alert(3L, "resolved")));
        when(users.find(anyLong())).thenReturn(Optional.empty());
        when(users.byId()).thenReturn(Map.of());
    }

    @Test
    void theReportCountsOnlyAlertsMatchingItsFilters() {
        AlertReportView v = service.get(5L);

        assertEquals(2, v.alertCount());
        assertEquals(List.of(), v.alertIds());
    }

    @Test
    void exportSendsTheIncludedAlertsAndCountsAnExport() {
        when(exporter.pdf(any(), any())).thenReturn(new byte[]{1, 2});

        assertArrayEquals(new byte[]{1, 2}, service.export(5L, true));

        verify(exporter).pdf(any(), argThat(l -> l.stream().map(AdminAlertView::id).toList().equals(List.of(1L, 3L))));
        verify(exporter, never()).csv(any(), any());
        assertEquals(1, report.getExportCount());
        verify(repository).save(report);
    }

    @Test
    void theStatsAddExportsAndActiveZones() {
        report.setExportCount(3);
        when(repository.all()).thenReturn(List.of(report));
        when(zones.active()).thenReturn(4L);

        var stats = service.stats();

        assertEquals(1, stats.totalReports());
        assertEquals(3, stats.totalAlerts());
        assertEquals(4, stats.activeZones());
        assertEquals(3, stats.exports());
    }
}
