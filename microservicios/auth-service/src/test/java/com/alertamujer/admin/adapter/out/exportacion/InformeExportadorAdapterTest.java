package com.alertamujer.admin.adapter.out.exportacion;

import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.alerts.api.AdminAlertView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InformeExportadorAdapterTest {

    private final InformeExportadorAdapter exporter = new InformeExportadorAdapter();

    private final AlertReportView informe = new AlertReportView(1L, "Informe", "Neiva",
            "pending", null, null, null, "PDF", List.of(), null, null, null, 1, 0, "Admin", null, null);

    private final List<AdminAlertView> alertas = List.of(new AdminAlertView(7L, 10L, 1L, "Ana", "ana@correo.com", 1L,
            "main", "panic_button", "active", "Ayuda; ya", new BigDecimal("2.9273"), new BigDecimal("-75.2819"),
            LocalDateTime.of(2026, 10, 15, 8, 30), null, null, null));

    @Test
    void csv_tieneBomCabeceraYFilasEscapadas() {
        String csv = new String(exporter.csv(informe, alertas), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("﻿ID;Inicio;"));
        // La hora sale en hora de Colombia, sin importar la zona del servidor
        String inicio = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .format(com.alertamujer.shared.web.HoraColombia.de(LocalDateTime.of(2026, 10, 15, 8, 30)));
        assertTrue(csv.contains("7;" + inicio + ";;Ana;ana@correo.com;Activa;Botón de pánico;\"Ayuda; ya\";2.9273;-75.2819"));
    }

    @Test
    void celdaCsv_neutralizaFormulas() {
        assertEquals("'=1+1", InformeExportadorAdapter.celdaCsv("=1+1"));
        assertEquals("-75.2819", InformeExportadorAdapter.celdaCsv("-75.2819"));
    }

    @Test
    void pdf_generaUnDocumento() {
        byte[] pdf = exporter.pdf(informe, alertas);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void duracion_enMinutosOHorasYVaciaSiSigueActiva() {
        AdminAlertView atendida = new AdminAlertView(8L, 10L, 1L, "Ana", null, 1L, "main", "panic_button", "resolved", null,
                null, null, LocalDateTime.of(2026, 10, 15, 8, 0), LocalDateTime.of(2026, 10, 15, 9, 5), null, null);
        AdminAlertView cancelada = new AdminAlertView(9L, 10L, 1L, "Ana", null, 1L, "main", "panic_button", "cancelled", null,
                null, null, LocalDateTime.of(2026, 10, 15, 8, 0), null, LocalDateTime.of(2026, 10, 15, 8, 12), null);

        assertEquals("1 h 05 min", InformeExportadorAdapter.duracion(atendida));
        assertEquals("12 min", InformeExportadorAdapter.duracion(cancelada));
        assertEquals("", InformeExportadorAdapter.duracion(alertas.get(0)));
    }

    @Test
    void pdf_conVariasPaginasYSinAlertas() {
        List<AdminAlertView> muchas = java.util.stream.IntStream.range(0, 80).mapToObj(i -> new AdminAlertView((long) i, 10L, 1L,
                "Ana", "ana@correo.com", 1L, "main", i % 2 == 0 ? "panic_button" : "contact", i % 3 == 0 ? "resolved" : "active",
                "Mensaje " + i, null, null, LocalDateTime.of(2026, 10, 15, 8, 0), null, null, null)).toList();

        assertEquals("%PDF", new String(exporter.pdf(informe, muchas), 0, 4, StandardCharsets.US_ASCII));
        assertEquals("%PDF", new String(exporter.pdf(informe, List.of()), 0, 4, StandardCharsets.US_ASCII));
    }
}
