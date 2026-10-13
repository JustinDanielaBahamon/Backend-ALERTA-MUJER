package com.alertamujer.evidence.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
 * Guarda los archivos de evidencia en disco con un nombre aleatorio (UUID), fuera de cualquier
 * carpeta pública. Solo la API los entrega, después de validar que la usuaria es la dueña.
 */
@Component
public class EvidenceStorage {

    public record StoredFile(String key, String sha256, long size) {}

    private final Path root;

    public EvidenceStorage(@Value("${evidence.storage-dir}") String storageDir) throws IOException {
        this.root = Path.of(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public StoredFile store(MultipartFile file) {
        String key = UUID.randomUUID().toString();
        Path destino = resolve(key);
        try (InputStream in = file.getInputStream();
             DigestInputStream digest = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            long size = Files.copy(digest, destino, StandardCopyOption.REPLACE_EXISTING);
            String sha256 = HexFormat.of().formatHex(digest.getMessageDigest().digest());
            return new StoredFile(key, sha256, size);
        } catch (IOException e) {
            delete(key);
            throw new UncheckedIOException("No se pudo guardar la evidencia", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Resource load(String key) {
        return new FileSystemResource(resolve(key));
    }

    public void delete(String key) {
        if (key == null) return;
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // Si no se puede borrar el archivo, no debe impedir borrar el registro
        }
    }

    // La clave siempre es un UUID generado aquí; se valida igual para impedir rutas tipo "../"
    private Path resolve(String key) {
        UUID.fromString(key);
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Clave de archivo inválida");
        return path;
    }
}
