package com.alertamujer.evidence.application.port.out;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Puerto de salida: dónde viven los archivos. Lo implementan el disco (desarrollo) y S3
 * (Backblaze B2 en producción); se elige con evidence.storage=disk|s3.
 */
public interface AlmacenamientoEvidencias {

    /** Archivo recibido, sin depender de cómo llegó (subida HTTP, pruebas...). Se puede leer varias veces. */
    record ArchivoEntrante(String nombreOriginal, String contentType, long tamano, Supplier<InputStream> contenido) {}

    record ArchivoGuardado(String key, String sha256, long size) {}

    /** Guarda el archivo con una clave aleatoria y calcula su huella SHA-256. */
    ArchivoGuardado guardar(ArchivoEntrante archivo);

    Resource cargar(String key);

    void eliminar(String key);

    /** Enlace temporal para abrirlo directo del almacenamiento (S3). Vacío si no los ofrece (disco). */
    default Optional<URI> enlaceTemporal(String key, String contentType, Duration vigencia) {
        return Optional.empty();
    }
}
