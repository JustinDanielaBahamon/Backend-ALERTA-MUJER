package com.alertamujer.evidence.infrastructure.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/**
 * Dónde viven los archivos de evidencia. En disco (desarrollo) o en un almacenamiento S3
 * (Backblaze B2 en producción, MinIO en local): el disco de Render se borra en cada despliegue.
 * Se elige con evidence.storage=disk|s3.
 */
public interface EvidenceStorage {

    record StoredFile(String key, String sha256, long size) {}

    /** Guarda el archivo con una clave aleatoria y calcula su huella SHA-256. */
    StoredFile store(MultipartFile file);

    Resource load(String key);

    void delete(String key);

    /**
     * Enlace temporal para abrir el archivo directamente desde el almacenamiento (S3), sin pasar
     * por el backend. Vacío si el almacenamiento no los ofrece (disco).
     */
    default Optional<URI> enlaceTemporal(String key, String contentType, Duration vigencia) {
        return Optional.empty();
    }
}
