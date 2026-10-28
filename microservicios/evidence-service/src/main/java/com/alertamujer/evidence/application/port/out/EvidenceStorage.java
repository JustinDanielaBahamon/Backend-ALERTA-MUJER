package com.alertamujer.evidence.application.port.out;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Output port: where the files live. Implemented by the disk (development) and S3 (Backblaze B2 in
 * production); chosen with evidence.storage=disk|s3.
 */
public interface EvidenceStorage {

    /** Received file, independent of how it arrived (HTTP upload, tests...). It can be read several times. */
    record IncomingFile(String originalName, String contentType, long size, Supplier<InputStream> content) {}

    record StoredFile(String key, String sha256, long size) {}

    /** Stores the file with a random key and computes its SHA-256 fingerprint. */
    StoredFile save(IncomingFile file);

    Resource load(String key);

    void delete(String key);

    /** Temporary link to open it straight from the storage (S3). Empty if not supported (disk). */
    default Optional<URI> temporaryLink(String key, String contentType, Duration validity) {
        return Optional.empty();
    }
}
