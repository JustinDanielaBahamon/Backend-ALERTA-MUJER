package com.alertamujer.admin.adapter.out.export;

import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.alerts.api.AdminAlertView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportExporterAdapterTest {

    private final ReportExporterAdapter exporter = new ReportExporterAdapter();

    private final AlertReportView report = new AlertReportView(1L, "Informe", "Neiva",
            "pending", null, null, null, "PDF", List.of(), null, null, null, 1, 0, "Admin", null, null);

    private final List<AdminAlertView> alerts = List.of(new AdminAlertView(7L, 10L, 1L, "Ana", "ana@correo.com", 1L,
            "main", "panic_button", "active", "Ayuda; ya", new BigDecimal("2.9273"), new BigDecimal("-75.2819"),
            LocalDateTime.of(2026, 10, 15, 8, 30), null, null, null));

    @Test
    void csv_hasBomHeaderAndEscapedRows() {
        String csv = new String(exporter.csv(report, alerts), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("﻿ID;Inicio;"));
        // La hora sale en hora de Colombia, sin importar la zona del servidor
        String start = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .format(com.alertamujer.shared.web.ColombiaTime.of(LocalDateTime.of(2026, 10, 15, 8, 30)));
        assertTrue(csv.contains("7;" + start + ";;Ana;ana@correo.com;Activa;Botón de pánico;\"Ayuda; ya\";2.9273;-75.2819"));
    }

    @Test
    void csvCell_neutralizesFormulas() {
        assertEquals("'=1+1", ReportExporterAdapter.csvCell("=1+1"));
        assertEquals("-75.2819", ReportExporterAdapter.csvCell("-75.2819"));
    }

    @Test
    void pdf_generatesADocument() {
        byte[] pdf = exporter.pdf(report, alerts);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void duration_inMinutesOrHoursAndEmptyWhileActive() {
        AdminAlertView attended = new AdminAlertView(8L, 10L, 1L, "Ana", null, 1L, "main", "panic_button", "resolved", null,
                null, null, LocalDateTime.of(2026, 10, 15, 8, 0), LocalDateTime.of(2026, 10, 15, 9, 5), null, null);
        AdminAlertView cancelled = new AdminAlertView(9L, 10L, 1L, "Ana", null, 1L, "main", "panic_button", "cancelled", null,
                null, null, LocalDateTime.of(2026, 10, 15, 8, 0), null, LocalDateTime.of(2026, 10, 15, 8, 12), null);

        assertEquals("1 h 05 min", ReportExporterAdapter.duration(attended));
        assertEquals("12 min", ReportExporterAdapter.duration(cancelled));
        assertEquals("", ReportExporterAdapter.duration(alerts.get(0)));
    }

    @Test
    void pdf_withSeveralPagesAndWithoutAlerts() {
        List<AdminAlertView> many = java.util.stream.IntStream.range(0, 80).mapToObj(i -> new AdminAlertView((long) i, 10L, 1L,
                "Ana", "ana@correo.com", 1L, "main", i % 2 == 0 ? "panic_button" : "contact", i % 3 == 0 ? "resolved" : "active",
                "Mensaje " + i, null, null, LocalDateTime.of(2026, 10, 15, 8, 0), null, null, null)).toList();

        assertEquals("%PDF", new String(exporter.pdf(report, many), 0, 4, StandardCharsets.US_ASCII));
        assertEquals("%PDF", new String(exporter.pdf(report, List.of()), 0, 4, StandardCharsets.US_ASCII));
    }
}
