package com.alertamujer.evidence.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evidencia de una alerta (foto, video, audio o documento). Es el modelo del dominio: no conoce la
 * base de datos ni Spring, y guarda las reglas propias de una evidencia (qué archivos se aceptan,
 * cómo se renombra, cuándo vence).
 */
public class Evidence {

    /** Revisión de la administración. */
    public static final Set<String> REVISIONES = Set.of("pending", "verified", "rejected");

    // SVG/HTML quedan fuera a propósito: pueden ejecutar scripts al abrirse
    private static final Set<String> DOCUMENTOS = Set.of("application/pdf");
    private static final int MAX_NOMBRE = 80;

    private Long id;
    private Long alertId;
    /** photo | video | audio | document */
    private String mediaType;
    /** Ruta de la API para obtener el archivo (nunca una URL pública). */
    private String fileUrl;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    /** Huella del archivo para demostrar que no fue alterado. */
    private String sha256;
    /** Nombre del archivo en el almacenamiento. Interno: no se expone. */
    private String storageKey;
    private String reviewStatus = "pending";
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Reglas ──────────────────────────────────────────────────────

    /** Tipo de evidencia según el archivo; rechaza lo que no es foto, video, audio o PDF. */
    public static String tipoDe(String contentType) {
        String c = contentType == null ? "" : contentType.toLowerCase();
        if (c.startsWith("image/") && !c.contains("svg")) return "photo";
        if (c.startsWith("video/")) return "video";
        if (c.startsWith("audio/")) return "audio";
        if (DOCUMENTOS.contains(c)) return "document";
        throw new IllegalArgumentException("Tipo de archivo no permitido. Sube una foto, video, audio o PDF.");
    }

    /** Nombre legible del archivo original; nunca se usa como ruta en el almacenamiento. */
    public static String nombreSeguro(String original, String mediaType) {
        String nombre = original == null ? "" : original.replaceAll("[\\\\/\\r\\n\"]", "_").trim();
        if (nombre.isEmpty()) nombre = mediaType;
        return nombre.length() > 200 ? nombre.substring(nombre.length() - 200) : nombre;
    }

    /** Cambia el nombre que ve la usuaria y conserva la extensión (el archivo y su huella no cambian). */
    public void renombrar(String nombre) {
        String limpio = nombre == null ? "" : nombre.replaceAll("[\\\\/\\r\\n\"<>]", " ").trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) throw new IllegalArgumentException("Escribe un nombre para la evidencia");
        if (limpio.length() > MAX_NOMBRE) {
            throw new IllegalArgumentException("El nombre puede tener máximo " + MAX_NOMBRE + " caracteres");
        }
        String extension = extension(fileName);
        if (!extension.isEmpty() && !limpio.toLowerCase().endsWith(extension.toLowerCase())) limpio += extension;
        this.fileName = limpio;
    }

    public void cambiarRevision(String revision) {
        if (revision == null || !REVISIONES.contains(revision)) {
            throw new IllegalArgumentException("Estado de revisión no válido: " + revision);
        }
        this.reviewStatus = revision;
    }

    /** true si la evidencia tiene un archivo guardado (las antiguas solo tenían una URL). */
    public boolean tieneArchivo() {
        return storageKey != null;
    }

    private static String extension(String nombre) {
        if (nombre == null) return "";
        int punto = nombre.lastIndexOf('.');
        return punto > 0 && nombre.length() - punto <= 6 ? nombre.substring(punto) : "";
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
