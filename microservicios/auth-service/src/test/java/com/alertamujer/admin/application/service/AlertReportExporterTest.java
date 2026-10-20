package com.alertamujer.admin.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertReportExporterTest {

    private final AlertReportExporter exporter = new AlertReportExporter();

    private final AlertReportService.AlertReportView informe = new AlertReportService.AlertReportView(1L, "Informe", "Neiva",
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
        assertEquals("'=1+1", AlertReportExporter.celdaCsv("=1+1"));
        assertEquals("-75.2819", AlertReportExporter.celdaCsv("-75.2819"));
    }

    @Test
    void pdf_generaUnDocumento() {
        byte[] pdf = exporter.pdf(informe, alertas);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }
}
