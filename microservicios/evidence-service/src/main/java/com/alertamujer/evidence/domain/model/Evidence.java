package com.alertamujer.evidence.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evidence of an alert (photo, video, audio or document). It is the domain model: it knows neither the
 * database nor Spring, and holds the evidence rules (which files are accepted, how it is renamed,
 * when it expires).
 */
public class Evidence {

    /** Review by the administration. */
    public static final Set<String> REVISIONES = Set.of("pending", "verified", "rejected");

    // SVG/HTML are left out on purpose: they can run scripts when opened
    private static final Set<String> DOCUMENTS = Set.of("application/pdf");
    private static final int MAX_NAME_LENGTH = 80;

    private Long id;
    private Long alertId;
    /** photo | video | audio | document */
    private String mediaType;
    /** API path to get the file (never a public URL). */
    private String fileUrl;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    /** File fingerprint to prove it was not altered. */
    private String sha256;
    /** File name in the storage. Internal: never exposed. */
    private String storageKey;
    private String reviewStatus = "pending";
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Reglas ──────────────────────────────────────────────────────

    /** Evidence type based on the file; rejects anything that is not a photo, video, audio or PDF. */
    public static String typeOf(String contentType) {
        String c = contentType == null ? "" : contentType.toLowerCase();
        if (c.startsWith("image/") && !c.contains("svg")) return "photo";
        if (c.startsWith("video/")) return "video";
        if (c.startsWith("audio/")) return "audio";
        if (DOCUMENTS.contains(c)) return "document";
        throw new IllegalArgumentException("Tipo de archivo no permitido. Sube una foto, video, audio o PDF.");
    }

    /** Readable name of the original file; never used as a storage path. */
    public static String safeName(String original, String mediaType) {
        String name = original == null ? "" : original.replaceAll("[\\\\/\\r\\n\"]", "_").trim();
        if (name.isEmpty()) name = mediaType;
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    /** Changes the name the user sees and keeps the extension (the file and its fingerprint do not change). */
    public void rename(String name) {
        String clean = name == null ? "" : name.replaceAll("[\\\\/\\r\\n\"<>]", " ").trim().replaceAll("\\s+", " ");
        if (clean.isEmpty()) throw new IllegalArgumentException("Escribe un nombre para la evidencia");
        if (clean.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("El nombre puede tener máximo " + MAX_NAME_LENGTH + " caracteres");
        }
        String extension = extension(fileName);
        if (!extension.isEmpty() && !clean.toLowerCase().endsWith(extension.toLowerCase())) clean += extension;
        this.fileName = clean;
    }

    public void changeReview(String review) {
        if (review == null || !REVISIONES.contains(review)) {
            throw new IllegalArgumentException("Estado de revisión no válido: " + review);
        }
        this.reviewStatus = review;
    }

    /** true if the evidence has a stored file (old ones only had a URL). */
    public boolean hasFile() {
        return storageKey != null;
    }

    private static String extension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot > 0 && name.length() - dot <= 6 ? name.substring(dot) : "";
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
