package com.alertamujer.admin.adapter.out.exportacion;

import com.alertamujer.admin.application.port.in.InformesUseCase.AlertReportView;
import com.alertamujer.admin.application.port.out.ExportadorInformesPort;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.shared.web.HoraColombia;
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
class InformeExportadorAdapter implements ExportadorInformesPort {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> ESTADOS = Map.of(
            "active", "Activa", "resolved", "Atendida", "cancelled", "Cancelada", "expired", "Expirada");
    private static final Map<String, String> METODOS = Map.of(
            "panic_button", "Botón de pánico", "contact", "Desde contactos", "suspicious_movement", "Movimiento sospechoso",
            "power_button", "Botón de encendido", "widget", "Widget");
    private static final String[] COLUMNAS = {
            "ID", "Inicio", "Fin", "Usuaria", "Correo", "Estado", "Activación", "Mensaje", "Latitud", "Longitud", "Duración"};

    /** CSV con separador ";" y BOM UTF-8, que es como Excel en español lo abre bien. */
    @Override
    public byte[] csv(AlertReportView informe, List<AdminAlertView> alertas) {
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

    @Override
    public byte[] pdf(AlertReportView informe, List<AdminAlertView> alertas) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 36);
        PdfWriter escritor = PdfWriter.getInstance(doc, salida);
        escritor.setPageEvent(new PiePagina());
        doc.open();

        // Encabezado: banda violeta con el nombre del informe
        PdfPTable banda = new PdfPTable(1);
        banda.setWidthPercentage(100);
        PdfPCell celdaBanda = new PdfPCell();
        celdaBanda.setBackgroundColor(VIOLETA);
        celdaBanda.setBorder(Rectangle.NO_BORDER);
        celdaBanda.setPadding(12);
        celdaBanda.addElement(new Paragraph("ALERTA MUJER · Informe de alertas", fuente(true, 9, LILA_CLARO)));
        celdaBanda.addElement(new Paragraph(informe.name(), fuente(true, 17, Color.WHITE)));
        banda.addCell(celdaBanda);
        doc.add(banda);
        doc.add(espacio(8));

        // Datos del informe en dos columnas
        PdfPTable datos = new PdfPTable(new float[]{1, 3, 1, 3});
        datos.setWidthPercentage(100);
        String periodo = informe.fromDate() == null && informe.toDate() == null ? null
                : (informe.fromDate() == null ? "…" : FECHA.format(HoraColombia.de(informe.fromDate()))) + " a "
                + (informe.toDate() == null ? "…" : FECHA.format(HoraColombia.de(informe.toDate())));
        String[][] pares = {
                {"Generado", FECHA.format(HoraColombia.de(LocalDateTime.now()))}, {"Creado por", informe.createdByName()},
                {"Ciudad", informe.city()}, {"Periodo", periodo},
                {"Usuaria", informe.userLabel()}, {"Acontecimiento", informe.event()},
        };
        for (String[] par : pares) {
            datos.addCell(sinBorde(par[0], fuente(true, 8.5f, GRIS)));
            datos.addCell(sinBorde(par[1] == null || par[1].isBlank() ? "—" : par[1], fuente(false, 9, TEXTO)));
        }
        doc.add(datos);
        if (informe.description() != null && !informe.description().isBlank()) {
            doc.add(espacio(4));
            doc.add(new Paragraph(informe.description(), fuente(false, 9, TEXTO)));
        }
        doc.add(espacio(10));

        // Resumen: total, por estado y por forma de activación
        doc.add(new Paragraph("Resumen", fuente(true, 11, VIOLETA)));
        doc.add(espacio(4));
        PdfPTable resumen = new PdfPTable(new float[]{1, 1, 1});
        resumen.setWidthPercentage(100);
        resumen.addCell(cajaResumen("Total de alertas", List.<String[]>of(new String[]{"", String.valueOf(alertas.size())})));
        resumen.addCell(cajaResumen("Por estado", conteo(alertas, a -> ESTADOS.getOrDefault(a.status(), a.status()))));
        resumen.addCell(cajaResumen("Por activación", conteo(alertas, a -> METODOS.getOrDefault(a.activationMethod(), a.activationMethod()))));
        doc.add(resumen);
        String promedio = duracionPromedio(alertas);
        if (promedio != null) {
            doc.add(espacio(4));
            doc.add(new Paragraph("Duración promedio de las alertas terminadas: " + promedio, fuente(false, 8.5f, GRIS)));
        }
        doc.add(espacio(10));

        // Detalle: una fila por alerta, filas alternadas
        doc.add(new Paragraph("Alertas incluidas", fuente(true, 11, VIOLETA)));
        doc.add(espacio(4));
        String[] columnas = {"#", "Inicio", "Duración", "Usuaria", "Estado", "Activación", "Mensaje", "Ubicación"};
        PdfPTable tabla = new PdfPTable(new float[]{4, 10, 7, 17, 7, 10, 26, 11});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String c : columnas) {
            PdfPCell cel = new PdfPCell(new Phrase(c, fuente(true, 8, Color.WHITE)));
            cel.setBackgroundColor(VIOLETA);
            cel.setBorderColor(VIOLETA);
            cel.setPadding(5);
            tabla.addCell(cel);
        }
        int n = 0;
        for (AdminAlertView a : alertas) {
            Color fondo = n++ % 2 == 0 ? Color.WHITE : LILA_FILA;
            String usuaria = (a.userName() == null ? "" : a.userName()) + (a.userEmail() == null ? "" : "\n" + a.userEmail());
            String ubicacion = a.latitude() == null || a.longitude() == null ? "—"
                    : a.latitude().setScale(4, java.math.RoundingMode.HALF_UP).toPlainString() + ", "
                    + a.longitude().setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
            String[] valores = {String.valueOf(a.id()),
                    a.startedAt() == null ? "" : FECHA.format(HoraColombia.de(a.startedAt())),
                    duracion(a), usuaria,
                    ESTADOS.getOrDefault(a.status(), a.status() == null ? "" : a.status()),
                    METODOS.getOrDefault(a.activationMethod(), a.activationMethod() == null ? "" : a.activationMethod()),
                    a.message() == null ? "" : a.message(), ubicacion};
            for (String v : valores) {
                PdfPCell cel = new PdfPCell(new Phrase(v, fuente(false, 7.5f, TEXTO)));
                cel.setBackgroundColor(fondo);
                cel.setBorderColor(LILA_BORDE);
                cel.setPadding(4);
                tabla.addCell(cel);
            }
        }
        if (alertas.isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("El informe no incluye alertas.", fuente(false, 8, GRIS)));
            vacia.setColspan(columnas.length);
            vacia.setPadding(8);
            tabla.addCell(vacia);
        }
        doc.add(tabla);
        doc.close();
        return salida.toByteArray();
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
                duracion(a),
        };
    }

    // ── Apoyo del PDF ───────────────────────────────────────────────

    private static final Color VIOLETA = new Color(0x5B, 0x2A, 0x86);
    private static final Color LILA_CLARO = new Color(0xE9, 0xD5, 0xFF);
    private static final Color LILA_FILA = new Color(0xF7, 0xF0, 0xFD);
    private static final Color LILA_BORDE = new Color(0xE3, 0xD1, 0xF5);
    private static final Color TEXTO = new Color(0x1E, 0x1B, 0x4B);
    private static final Color GRIS = new Color(0x6B, 0x5B, 0x95);

    private static Font fuente(boolean negrita, float tamano, Color color) {
        return FontFactory.getFont(negrita ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, tamano, color);
    }

    private static Paragraph espacio(float alto) {
        Paragraph p = new Paragraph(" ", fuente(false, 2, Color.WHITE));
        p.setSpacingAfter(alto);
        return p;
    }

    private static PdfPCell sinBorde(String texto, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingBottom(4);
        return c;
    }

    private static PdfPCell cajaResumen(String titulo, List<String[]> filas) {
        PdfPCell caja = new PdfPCell();
        caja.setBorderColor(LILA_BORDE);
        caja.setBackgroundColor(LILA_FILA);
        caja.setPadding(8);
        caja.addElement(new Paragraph(titulo, fuente(true, 8.5f, GRIS)));
        if (filas.size() == 1 && filas.get(0)[0].isEmpty()) {
            caja.addElement(new Paragraph(filas.get(0)[1], fuente(true, 20, VIOLETA)));
        } else if (filas.isEmpty()) {
            caja.addElement(new Paragraph("—", fuente(false, 9, TEXTO)));
        } else {
            for (String[] f : filas) caja.addElement(new Paragraph(f[0] + ": " + f[1], fuente(false, 9, TEXTO)));
        }
        return caja;
    }

    private static List<String[]> conteo(List<AdminAlertView> alertas, java.util.function.Function<AdminAlertView, String> clave) {
        Map<String, Long> cuenta = new java.util.TreeMap<>();
        for (AdminAlertView a : alertas) cuenta.merge(clave.apply(a) == null ? "Sin dato" : clave.apply(a), 1L, Long::sum);
        return cuenta.entrySet().stream().map(e -> new String[]{e.getKey(), String.valueOf(e.getValue())}).toList();
    }

    private static LocalDateTime fin(AdminAlertView a) {
        return a.endedAt() != null ? a.endedAt() : a.cancelledAt();
    }

    /** "12 min", "1 h 05 min"; vacío si la alerta sigue activa. */
    static String duracion(AdminAlertView a) {
        LocalDateTime fin = fin(a);
        if (a.startedAt() == null || fin == null) return "";
        return formatoMinutos(Math.max(0, java.time.Duration.between(a.startedAt(), fin).toMinutes()));
    }

    private static String duracionPromedio(List<AdminAlertView> alertas) {
        var minutos = alertas.stream().filter(a -> a.startedAt() != null && fin(a) != null)
                .mapToLong(a -> Math.max(0, java.time.Duration.between(a.startedAt(), fin(a)).toMinutes())).toArray();
        if (minutos.length == 0) return null;
        return formatoMinutos(Math.round(java.util.Arrays.stream(minutos).average().orElse(0)));
    }

    private static String formatoMinutos(long min) {
        return min < 60 ? min + " min" : (min / 60) + " h " + String.format("%02d", min % 60) + " min";
    }

    /** Pie de cada página: aviso de confidencialidad y número de página. */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase texto = new Phrase("Documento confidencial · Contiene datos personales de usuarias de AlertaMujer · Página "
                    + writer.getPageNumber(), fuente(false, 7.5f, GRIS));
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, texto,
                    (document.left() + document.right()) / 2, document.bottom() - 18, 0);
        }
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
