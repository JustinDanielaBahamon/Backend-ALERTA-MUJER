package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AlertReport;
import com.alertamujer.admin.infrastructure.repository.AlertReportRepository;
import com.alertamujer.alerts.application.service.AdminAlertService;
import com.alertamujer.alerts.application.service.AdminAlertService.AdminAlertView;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.zones.infrastructure.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertReportServiceTest {

    @Mock
    private AlertReportRepository alertReportRepository;

    @Mock
    private AdminAlertService adminAlertService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private AlertReportService service;

    private static final LocalDateTime HOY = LocalDateTime.of(2026, 10, 15, 12, 0);

    private AdminAlertView alerta(long id, LocalDateTime inicio, String estado) {
        return new AdminAlertView(id, 10L, 1L, "Ana", "ana@correo.com", 1L, "main", "panic_button", estado,
                "SOS", null, null, inicio, null, null, inicio);
    }

    private final List<AdminAlertView> alertas = List.of(
            alerta(1, HOY.minusDays(10), "resolved"),
            alerta(2, HOY.minusDays(2), "active"),
            alerta(3, HOY, "resolved"));

    @Test
    void alertasDe_usaLasElegidasSiHay() {
        AlertReport r = new AlertReport();
        r.setAlertIds("1,3");
        r.setAlertStatus("active"); // se ignora: mandan las elegidas

        assertEquals(List.of(1L, 3L), AlertReportService.alertasDe(r, alertas).stream().map(AdminAlertView::id).toList());
    }

    @Test
    void alertasDe_sinElegidasAplicaFechasYEstado() {
        AlertReport r = new AlertReport();
        r.setFromDate(HOY.minusDays(5));
        r.setAlertStatus("resolved");

        assertEquals(List.of(3L), AlertReportService.alertasDe(r, alertas).stream().map(AdminAlertView::id).toList());
    }

    @Test
    void crear_guardaLaCreadoraYLasAlertas() {
        when(alertReportRepository.save(any(AlertReport.class))).thenAnswer(inv -> inv.getArgument(0));
        when(adminAlertService.listar()).thenReturn(alertas);
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        AlertReportService.AlertReportView vista = service.crear(new AlertReportService.AlertReportRequest(
                "Informe octubre", "Neiva", null, null, null, null, "EXCEL", List.of(2L, 1L), null, null, null), 9L);

        assertEquals("Informe octubre", vista.name());
        assertEquals("pending", vista.status());
        assertEquals("EXCEL", vista.format());
        assertEquals(List.of(1L, 2L), vista.alertIds());
        assertEquals(2, vista.alertCount());
    }

    @Test
    void crear_rechazaValoresNoValidos() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(new AlertReportService.AlertReportRequest(
                " ", null, null, null, null, null, null, null, null, null, null), 9L));
        assertThrows(IllegalArgumentException.class, () -> service.crear(new AlertReportService.AlertReportRequest(
                "x", null, "Cerrada", null, null, null, null, null, null, null, null), 9L));
    }

    @Test
    void registrarExportacion_sumaUno() {
        AlertReport r = new AlertReport();
        r.setId(1L);
        r.setExportCount(2);
        when(alertReportRepository.findById(1L)).thenReturn(Optional.of(r));

        service.registrarExportacion(1L);

        assertEquals(3, r.getExportCount());
        verify(alertReportRepository).save(r);
    }
}
