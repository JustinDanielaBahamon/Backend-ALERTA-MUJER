package com.alertamujer.admin.adapter.out.export;

import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.admin.application.port.out.ReportExporterPort;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.shared.web.ColombiaTime;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Adaptador de salida: genera el archivo de un informe de alertas, PDF (OpenPDF) o CSV para abrir en Excel. */
@Component
class ReportExporterAdapter implements ReportExporterPort {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> STATUSES = Map.of(
            "active", "Activa", "resolved", "Atendida", "cancelled", "Cancelada", "expired", "Expirada");
    private static final Map<String, String> METHODS = Map.of(
            "panic_button", "Botón de pánico", "contact", "Desde contactos", "suspicious_movement", "Movimiento sospechoso",
            "power_button", "Botón de encendido", "widget", "Widget");
    private static final String[] COLUMNS = {
            "ID", "Inicio", "Fin", "Usuaria", "Correo", "Estado", "Activación", "Mensaje", "Latitud", "Longitud", "Duración"};

    /** CSV con separador ";" y BOM UTF-8, que es como Excel en español lo abre bien. */
    @Override
    public byte[] csv(AlertReportView report, List<AdminAlertView> alerts) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(";", COLUMNS)).append("\r\n");
        for (AdminAlertView a : alerts) {
            String[] row = row(a);
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(';');
                sb.append(csvCell(row[i]));
            }
            sb.append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] pdf(AlertReportView report, List<AdminAlertView> alerts) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, output);
        writer.setPageEvent(new PageFooter());
        doc.open();

        // Encabezado: banda violeta con el nombre del informe
        PdfPTable band = new PdfPTable(1);
        band.setWidthPercentage(100);
        PdfPCell bandCell = new PdfPCell();
        bandCell.setBackgroundColor(VIOLET);
        bandCell.setBorder(Rectangle.NO_BORDER);
        bandCell.setPadding(12);
        bandCell.addElement(new Paragraph("ALERTA MUJER · Informe de alertas", font(true, 9, LIGHT_LILAC)));
        bandCell.addElement(new Paragraph(report.name(), font(true, 17, Color.WHITE)));
        band.addCell(bandCell);
        doc.add(band);
        doc.add(spacer(8));

        // Datos del informe en dos columnas
        PdfPTable data = new PdfPTable(new float[]{1, 3, 1, 3});
        data.setWidthPercentage(100);
        String period = report.fromDate() == null && report.toDate() == null ? null
                : (report.fromDate() == null ? "…" : DATE.format(ColombiaTime.of(report.fromDate()))) + " a "
                + (report.toDate() == null ? "…" : DATE.format(ColombiaTime.of(report.toDate())));
        String[][] pares = {
                {"Generado", DATE.format(ColombiaTime.of(LocalDateTime.now()))}, {"Creado por", report.createdByName()},
                {"Ciudad", report.city()}, {"Periodo", period},
                {"Usuaria", report.userLabel()}, {"Acontecimiento", report.event()},
        };
        for (String[] par : pares) {
            data.addCell(noBorder(par[0], font(true, 8.5f, GRIS)));
            data.addCell(noBorder(par[1] == null || par[1].isBlank() ? "—" : par[1], font(false, 9, TEXT)));
        }
        doc.add(data);
        if (report.description() != null && !report.description().isBlank()) {
            doc.add(spacer(4));
            doc.add(new Paragraph(report.description(), font(false, 9, TEXT)));
        }
        doc.add(spacer(10));

        // Resumen: total, por estado y por forma de activación
        doc.add(new Paragraph("Resumen", font(true, 11, VIOLET)));
        doc.add(spacer(4));
        PdfPTable summary = new PdfPTable(new float[]{1, 1, 1});
        summary.setWidthPercentage(100);
        summary.addCell(cajaResumen("Total de alertas", List.<String[]>of(new String[]{"", String.valueOf(alerts.size())})));
        summary.addCell(cajaResumen("Por estado", counts(alerts, a -> STATUSES.getOrDefault(a.status(), a.status()))));
        summary.addCell(cajaResumen("Por activación", counts(alerts, a -> METHODS.getOrDefault(a.activationMethod(), a.activationMethod()))));
        doc.add(summary);
        String average = averageDuration(alerts);
        if (average != null) {
            doc.add(spacer(4));
            doc.add(new Paragraph("Duración promedio de las alertas terminadas: " + average, font(false, 8.5f, GRIS)));
        }
        doc.add(spacer(10));

        // Detalle: una fila por alerta, filas alternadas
        doc.add(new Paragraph("Alertas incluidas", font(true, 11, VIOLET)));
        doc.add(spacer(4));
        String[] columns = {"#", "Inicio", "Duración", "Usuaria", "Estado", "Activación", "Mensaje", "Ubicación"};
        PdfPTable table = new PdfPTable(new float[]{4, 10, 7, 17, 7, 10, 26, 11});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        for (String c : columns) {
            PdfPCell phone = new PdfPCell(new Phrase(c, font(true, 8, Color.WHITE)));
            phone.setBackgroundColor(VIOLET);
            phone.setBorderColor(VIOLET);
            phone.setPadding(5);
            table.addCell(phone);
        }
        int n = 0;
        for (AdminAlertView a : alerts) {
            Color background = n++ % 2 == 0 ? Color.WHITE : LILAC_ROW;
            String user = (a.userName() == null ? "" : a.userName()) + (a.userEmail() == null ? "" : "\n" + a.userEmail());
            String location = a.latitude() == null || a.longitude() == null ? "—"
                    : a.latitude().setScale(4, java.math.RoundingMode.HALF_UP).toPlainString() + ", "
                    + a.longitude().setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
            String[] valores = {String.valueOf(a.id()),
                    a.startedAt() == null ? "" : DATE.format(ColombiaTime.of(a.startedAt())),
                    duration(a), user,
                    STATUSES.getOrDefault(a.status(), a.status() == null ? "" : a.status()),
                    METHODS.getOrDefault(a.activationMethod(), a.activationMethod() == null ? "" : a.activationMethod()),
                    a.message() == null ? "" : a.message(), location};
            for (String v : valores) {
                PdfPCell phone = new PdfPCell(new Phrase(v, font(false, 7.5f, TEXT)));
                phone.setBackgroundColor(background);
                phone.setBorderColor(LILAC_BORDER);
                phone.setPadding(4);
                table.addCell(phone);
            }
        }
        if (alerts.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("El informe no incluye alertas.", font(false, 8, GRIS)));
            empty.setColspan(columns.length);
            empty.setPadding(8);
            table.addCell(empty);
        }
        doc.add(table);
        doc.close();
        return output.toByteArray();
    }

    private static String[] row(AdminAlertView a) {
        return new String[]{
                String.valueOf(a.id()),
                a.startedAt() == null ? "" : DATE.format(ColombiaTime.of(a.startedAt())),
                a.endedAt() != null ? DATE.format(ColombiaTime.of(a.endedAt())) : a.cancelledAt() != null ? DATE.format(ColombiaTime.of(a.cancelledAt())) : "",
                a.userName() == null ? "" : a.userName(),
                a.userEmail() == null ? "" : a.userEmail(),
                STATUSES.getOrDefault(a.status(), a.status() == null ? "" : a.status()),
                METHODS.getOrDefault(a.activationMethod(), a.activationMethod() == null ? "" : a.activationMethod()),
                a.message() == null ? "" : a.message(),
                a.latitude() == null ? "" : a.latitude().toPlainString(),
                a.longitude() == null ? "" : a.longitude().toPlainString(),
                duration(a),
        };
    }

    // ── Apoyo del PDF ───────────────────────────────────────────────

    private static final Color VIOLET = new Color(0x5B, 0x2A, 0x86);
    private static final Color LIGHT_LILAC = new Color(0xE9, 0xD5, 0xFF);
    private static final Color LILAC_ROW = new Color(0xF7, 0xF0, 0xFD);
    private static final Color LILAC_BORDER = new Color(0xE3, 0xD1, 0xF5);
    private static final Color TEXT = new Color(0x1E, 0x1B, 0x4B);
    private static final Color GRIS = new Color(0x6B, 0x5B, 0x95);

    private static Font font(boolean bold, float size, Color color) {
        return FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size, color);
    }

    private static Paragraph spacer(float alto) {
        Paragraph p = new Paragraph(" ", font(false, 2, Color.WHITE));
        p.setSpacingAfter(alto);
        return p;
    }

    private static PdfPCell noBorder(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingBottom(4);
        return c;
    }

    private static PdfPCell cajaResumen(String title, List<String[]> rows) {
        PdfPCell caja = new PdfPCell();
        caja.setBorderColor(LILAC_BORDER);
        caja.setBackgroundColor(LILAC_ROW);
        caja.setPadding(8);
        caja.addElement(new Paragraph(title, font(true, 8.5f, GRIS)));
        if (rows.size() == 1 && rows.get(0)[0].isEmpty()) {
            caja.addElement(new Paragraph(rows.get(0)[1], font(true, 20, VIOLET)));
        } else if (rows.isEmpty()) {
            caja.addElement(new Paragraph("—", font(false, 9, TEXT)));
        } else {
            for (String[] f : rows) caja.addElement(new Paragraph(f[0] + ": " + f[1], font(false, 9, TEXT)));
        }
        return caja;
    }

    private static List<String[]> counts(List<AdminAlertView> alerts, java.util.function.Function<AdminAlertView, String> key) {
        Map<String, Long> account = new java.util.TreeMap<>();
        for (AdminAlertView a : alerts) account.merge(key.apply(a) == null ? "Sin dato" : key.apply(a), 1L, Long::sum);
        return account.entrySet().stream().map(e -> new String[]{e.getKey(), String.valueOf(e.getValue())}).toList();
    }

    private static LocalDateTime fin(AdminAlertView a) {
        return a.endedAt() != null ? a.endedAt() : a.cancelledAt();
    }

    /** "12 min", "1 h 05 min"; vacío si la alerta sigue activa. */
    static String duration(AdminAlertView a) {
        LocalDateTime fin = fin(a);
        if (a.startedAt() == null || fin == null) return "";
        return formatMinutes(Math.max(0, java.time.Duration.between(a.startedAt(), fin).toMinutes()));
    }

    private static String averageDuration(List<AdminAlertView> alerts) {
        var minutes = alerts.stream().filter(a -> a.startedAt() != null && fin(a) != null)
                .mapToLong(a -> Math.max(0, java.time.Duration.between(a.startedAt(), fin(a)).toMinutes())).toArray();
        if (minutes.length == 0) return null;
        return formatMinutes(Math.round(java.util.Arrays.stream(minutes).average().orElse(0)));
    }

    private static String formatMinutes(long min) {
        return min < 60 ? min + " min" : (min / 60) + " h " + String.format("%02d", min % 60) + " min";
    }

    /** Pie de cada página: aviso de confidencialidad y número de página. */
    private static class PageFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase text = new Phrase("Documento confidencial · Contiene datos personales de usuarias de AlertaMujer · Página "
                    + writer.getPageNumber(), font(false, 7.5f, GRIS));
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, text,
                    (document.left() + document.right()) / 2, document.bottom() - 18, 0);
        }
    }

    /** Comillas si hace falta y protección contra fórmulas (inyección CSV). */
    static String csvCell(String value) {
        String v = value == null ? "" : value;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0 && !v.matches("-?\\d+(\\.\\d+)?")) v = "'" + v;
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
