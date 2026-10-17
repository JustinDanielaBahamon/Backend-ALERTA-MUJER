package com.alertamujer.evidence.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Evidencias en un almacenamiento compatible con S3: Backblaze B2 en producción y MinIO en local.
 * El bucket es privado; los archivos solo se leen por la API o con enlaces firmados que vencen.
 */
@Component
@ConditionalOnProperty(name = "evidence.storage", havingValue = "s3")
public class S3EvidenceStorage implements EvidenceStorage {

    private static final Logger log = LoggerFactory.getLogger(S3EvidenceStorage.class);
    private static final String PREFIJO = "evidencias/";

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public S3EvidenceStorage(@Value("${evidence.s3.endpoint}") String endpoint,
                             @Value("${evidence.s3.region}") String region,
                             @Value("${evidence.s3.bucket}") String bucket,
                             @Value("${evidence.s3.access-key}") String accessKey,
                             @Value("${evidence.s3.secret-key}") String secretKey,
                             @Value("${evidence.s3.create-bucket:false}") boolean crearBucket,
                             @Value("${evidence.s3.public-endpoint:}") String endpointPublico) {
        if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("evidence.storage=s3 requiere EVIDENCE_S3_ENDPOINT, _BUCKET, _ACCESS_KEY y _SECRET_KEY");
        }
        var credenciales = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        // Rutas tipo endpoint/bucket/clave: las aceptan B2 y MinIO
        var configuracion = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        this.s3 = S3Client.builder()
                .httpClient(UrlConnectionHttpClient.create())
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(credenciales)
                .serviceConfiguration(configuracion)
                // Las sumas CRC32 automáticas del SDK nuevo no las aceptan todos los proveedores
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        // Los enlaces firmados los abre el navegador del contacto: en Docker local el backend ve
        // "minio:9000" pero el navegador "localhost:9000"
        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpointPublico.isBlank() ? endpoint : endpointPublico))
                .region(Region.of(region))
                .credentialsProvider(credenciales)
                .serviceConfiguration(configuracion)
                .build();
        this.bucket = bucket;
        if (crearBucket) crearBucketSiFalta();
    }

    @Override
    public StoredFile store(MultipartFile file) {
        String key = UUID.randomUUID().toString();
        try {
            String sha256 = huella(file);
            // El SDK puede leer el contenido más de una vez (firma, reintentos): se le da una forma
            // de abrirlo de nuevo, que la subida multipart permite (queda en memoria o en disco)
            ContentStreamProvider contenido = () -> {
                try {
                    return file.getInputStream();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
            s3.putObject(b -> b.bucket(bucket).key(PREFIJO + key).contentType(file.getContentType()),
                    RequestBody.fromContentProvider(contenido, file.getSize(), file.getContentType()));
            return new StoredFile(key, sha256, file.getSize());
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("No se pudo leer la evidencia", e);
        } catch (S3Exception e) {
            log.error("No se pudo subir la evidencia al almacenamiento: {}", e.awsErrorDetails().errorMessage());
            throw new IllegalStateException("No se pudo guardar la evidencia");
        }
    }

    private static String huella(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream();
             DigestInputStream digest = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            digest.transferTo(OutputStream.nullOutputStream());
            return HexFormat.of().formatHex(digest.getMessageDigest().digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Resource load(String key) {
        return new ObjetoS3(validar(key));
    }

    @Override
    public void delete(String key) {
        if (key == null) return;
        try {
            s3.deleteObject(b -> b.bucket(bucket).key(PREFIJO + validar(key)));
        } catch (S3Exception e) {
            // Si no se puede borrar el archivo, no debe impedir borrar el registro
            log.warn("No se pudo borrar la evidencia {} del almacenamiento: {}", key, e.getMessage());
        }
    }

    @Override
    public Optional<URI> enlaceTemporal(String key, String contentType, Duration vigencia) {
        String clave = PREFIJO + validar(key);
        var firmado = presigner.presignGetObject(p -> p
                .signatureDuration(vigencia)
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(clave)
                        .responseContentType(contentType)
                        .responseContentDisposition("inline")
                        .build()));
        return Optional.of(URI.create(firmado.url().toString()));
    }

    // La clave siempre es un UUID generado aquí; se valida igual para no tocar otros objetos
    private static String validar(String key) {
        return UUID.fromString(key).toString();
    }

    private void crearBucketSiFalta() {
        try {
            s3.headBucket(b -> b.bucket(bucket));
        } catch (NoSuchBucketException e) {
            s3.createBucket(b -> b.bucket(bucket));
            log.info("Bucket de evidencias {} creado", bucket);
        } catch (S3Exception e) {
            if (e.statusCode() != 404) throw e;
            s3.createBucket(b -> b.bucket(bucket));
            log.info("Bucket de evidencias {} creado", bucket);
        }
    }

    /** Objeto del bucket como Resource: Spring lo entrega con soporte de Range (adelantar videos). */
    private final class ObjetoS3 extends AbstractResource {
        private final String key;
        private HeadObjectResponse cabecera;

        private ObjetoS3(String key) {
            this.key = key;
        }

        private Optional<HeadObjectResponse> cabecera() {
            if (cabecera == null) {
                try {
                    cabecera = s3.headObject(b -> b.bucket(bucket).key(PREFIJO + key));
                } catch (NoSuchKeyException e) {
                    return Optional.empty();
                } catch (S3Exception e) {
                    if (e.statusCode() == 404) return Optional.empty();
                    throw e;
                }
            }
            return Optional.of(cabecera);
        }

        @Override
        public boolean exists() {
            return cabecera().isPresent();
        }

        @Override
        public long contentLength() throws IOException {
            return cabecera().map(HeadObjectResponse::contentLength)
                    .orElseThrow(() -> new IOException("La evidencia no existe en el almacenamiento"));
        }

        @Override
        public InputStream getInputStream() {
            return s3.getObject(b -> b.bucket(bucket).key(PREFIJO + key));
        }

        @Override
        public String getFilename() {
            return key;
        }

        @Override
        public String getDescription() {
            return "Evidencia " + key + " en el almacenamiento S3";
        }
    }
}
