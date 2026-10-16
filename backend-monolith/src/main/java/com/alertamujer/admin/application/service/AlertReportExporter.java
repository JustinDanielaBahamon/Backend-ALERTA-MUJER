package com.alertamujer.admin.application.service;

import com.alertamujer.shared.web.HoraColombia;
import com.alertamujer.alerts.api.AdminAlertView;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Genera el archivo de un informe de alertas: PDF, o CSV para abrir en Excel. */
@Component
public class AlertReportExporter {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> ESTADOS = Map.of(
            "active", "Activa", "resolved", "Atendida", "cancelled", "Cancelada", "expired", "Expirada");
    private static final Map<String, String> METODOS = Map.of(
            "panic_button", "Botón de pánico", "contact", "Desde contactos");
    private static final String[] COLUMNAS = {
            "ID", "Inicio", "Fin", "Usuaria", "Correo", "Estado", "Activación", "Mensaje", "Latitud", "Longitud"};

    /** CSV con separador ";" y BOM UTF-8, que es como Excel en español lo abre bien. */
    public byte[] csv(AlertReportService.AlertReportView informe, List<AdminAlertView> alertas) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(";", COLUMNAS)).append("\r\n");
        for (AdminAlertView a : alertas) {
            String[] fila = fila(a);
            for (int i = 0; i < fila.length; i++) {
                if (i > 0) sb.append(';');
                sb.append(celdaCsv(fila[i]));
            }
            sb.append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] pdf(AlertReportService.AlertReportView informe, List<AdminAlertView> alertas) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
        PdfWriter.getInstance(doc, salida);
        doc.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0x5B, 0x2A, 0x86));
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        Font celda = FontFactory.getFont(FontFactory.HELVETICA, 8);

        doc.add(new Paragraph("ALERTA MUJER — " + informe.name(), titulo));
        doc.add(new Paragraph(" "));
        dato(doc, "Generado", FECHA.format(HoraColombia.de(LocalDateTime.now())), negrita, normal);
        dato(doc, "Creado por", informe.createdByName(), negrita, normal);
        dato(doc, "Ciudad", informe.city(), negrita, normal);
        dato(doc, "Usuaria", informe.userLabel(), negrita, normal);
        dato(doc, "Acontecimiento", informe.event(), negrita, normal);
        dato(doc, "Descripción", informe.description(), negrita, normal);
        if (informe.fromDate() != null || informe.toDate() != null) {
            dato(doc, "Periodo", (informe.fromDate() == null ? "…" : FECHA.format(HoraColombia.de(informe.fromDate()))) + " a "
                    + (informe.toDate() == null ? "…" : FECHA.format(HoraColombia.de(informe.toDate()))), negrita, normal);
        }
        dato(doc, "Total de alertas", String.valueOf(alertas.size()), negrita, normal);
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(new float[]{4, 9, 9, 12, 14, 7, 9, 22, 7, 7});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String c : COLUMNAS) {
            PdfPCell cel = new PdfPCell(new Phrase(c, cabecera));
            cel.setBackgroundColor(new Color(0x5B, 0x2A, 0x86));
            cel.setHorizontalAlignment(Element.ALIGN_CENTER);
            cel.setPadding(4);
            tabla.addCell(cel);
        }
        for (AdminAlertView a : alertas) {
            for (String v : fila(a)) {
                PdfPCell cel = new PdfPCell(new Phrase(v, celda));
                cel.setPadding(3);
                tabla.addCell(cel);
            }
        }
        if (alertas.isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("El informe no incluye alertas.", celda));
            vacia.setColspan(COLUMNAS.length);
            vacia.setPadding(6);
            tabla.addCell(vacia);
        }
        doc.add(tabla);
        doc.close();
        return salida.toByteArray();
    }

    private static void dato(Document doc, String etiqueta, String valor, Font negrita, Font normal) {
        if (valor == null || valor.isBlank()) return;
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta + ": ", negrita));
        p.add(new Phrase(valor, normal));
        doc.add(p);
    }

    private static String[] fila(AdminAlertView a) {
        return new String[]{
                String.valueOf(a.id()),
                a.startedAt() == null ? "" : FECHA.format(HoraColombia.de(a.startedAt())),
                a.endedAt() != null ? FECHA.format(HoraColombia.de(a.endedAt())) : a.cancelledAt() != null ? FECHA.format(HoraColombia.de(a.cancelledAt())) : "",
                a.userName() == null ? "" : a.userName(),
                a.userEmail() == null ? "" : a.userEmail(),
                ESTADOS.getOrDefault(a.status(), a.status() == null ? "" : a.status()),
                METODOS.getOrDefault(a.activationMethod(), a.activationMethod() == null ? "" : a.activationMethod()),
                a.message() == null ? "" : a.message(),
                a.latitude() == null ? "" : a.latitude().toPlainString(),
                a.longitude() == null ? "" : a.longitude().toPlainString(),
        };
    }

    /** Comillas si hace falta y protección contra fórmulas (inyección CSV). */
    static String celdaCsv(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0 && !v.matches("-?\\d+(\\.\\d+)?")) v = "'" + v;
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
