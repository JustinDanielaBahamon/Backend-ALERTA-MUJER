package com.alertamujer.evidence.infrastructure.storage;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sin red: comprueba que el cliente S3 se puede crear (dependencias) y que firma enlaces. */
class S3EvidenceStorageTest {

    private static final String KEY = "6f1c2a0e-3b7d-4c55-9a64-2b0f3d9e1c11";

    private S3EvidenceStorage crear() {
        return new S3EvidenceStorage("http://s3:8333", "us-east-1", "evidencias", "clave", "secreto", false,
                "http://localhost:8333");
    }

    @Test
    void firmaEnlacesTemporalesConElEndpointPublico() {
        String url = crear().enlaceTemporal(KEY, "video/mp4", Duration.ofMinutes(10)).orElseThrow().toString();

        assertTrue(url.startsWith("http://localhost:8333/evidencias/evidencias/" + KEY));
        assertTrue(url.contains("X-Amz-Signature="));
        assertTrue(url.contains("X-Amz-Expires=600"));
    }

    @Test
    void rechazaClavesQueNoSonSuyas() {
        assertThrows(IllegalArgumentException.class,
                () -> crear().enlaceTemporal("../otra-cosa", "video/mp4", Duration.ofMinutes(1)));
    }

    @Test
    void exigeLaConfiguracionCompleta() {
        assertThrows(IllegalStateException.class,
                () -> new S3EvidenceStorage("", "us-east-1", "evidencias", "clave", "secreto", false, ""));
    }
}
