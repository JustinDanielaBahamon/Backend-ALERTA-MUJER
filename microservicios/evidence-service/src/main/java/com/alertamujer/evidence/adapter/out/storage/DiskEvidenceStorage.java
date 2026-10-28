package com.alertamujer.evidence.adapter.out.storage;

import com.alertamujer.evidence.application.port.out.EvidenceStorage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Stores evidence files on disk with a random name (UUID), outside any public folder. Only the API
 * serves them, after checking that the user is the owner.
 * It is the default storage (development and tests); production uses S3.
 */
@Component
@ConditionalOnProperty(name = "evidence.storage", havingValue = "disk", matchIfMissing = true)
public class DiskEvidenceStorage implements EvidenceStorage {

    private final Path root;

    public DiskEvidenceStorage(@Value("${evidence.storage-dir}") String storageDir) throws IOException {
        this.root = Path.of(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public StoredFile save(IncomingFile file) {
        String key = UUID.randomUUID().toString();
        Path target = resolve(key);
        try (InputStream in = file.content().get();
             DigestInputStream digest = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            long size = Files.copy(digest, target, StandardCopyOption.REPLACE_EXISTING);
            String sha256 = HexFormat.of().formatHex(digest.getMessageDigest().digest());
            return new StoredFile(key, sha256, size);
        } catch (IOException e) {
            delete(key);
            throw new UncheckedIOException("No se pudo guardar la evidencia", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Resource load(String key) {
        return new FileSystemResource(resolve(key));
    }

    @Override
    public void delete(String key) {
        if (key == null) return;
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // If the file cannot be deleted, that must not prevent deleting the record
        }
    }

    // The key is always a UUID generated here; it is validated anyway to block paths like "../"
    private Path resolve(String key) {
        UUID.fromString(key);
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Clave de archivo inválida");
        return path;
    }
}
