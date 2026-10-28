package com.alertamujer.evidence.adapter.out.storage;

import com.alertamujer.evidence.application.port.out.EvidenceStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
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
 * Evidence in S3-compatible storage: Backblaze B2 in production and MinIO locally.
 * The bucket is private; files are only read through the API or with signed links that expire.
 */
@Component
@ConditionalOnProperty(name = "evidence.storage", havingValue = "s3")
public class S3EvidenceStorage implements EvidenceStorage {

    private static final Logger log = LoggerFactory.getLogger(S3EvidenceStorage.class);
    private static final String PREFIX = "evidencias/";

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public S3EvidenceStorage(@Value("${evidence.s3.endpoint}") String endpoint,
                             @Value("${evidence.s3.region}") String region,
                             @Value("${evidence.s3.bucket}") String bucket,
                             @Value("${evidence.s3.access-key}") String accessKey,
                             @Value("${evidence.s3.secret-key}") String secretKey,
                             @Value("${evidence.s3.create-bucket:false}") boolean createBucket,
                             @Value("${evidence.s3.public-endpoint:}") String publicEndpoint) {
        if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("evidence.storage=s3 requiere EVIDENCE_S3_ENDPOINT, _BUCKET, _ACCESS_KEY y _SECRET_KEY");
        }
        var credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        // Rutas tipo endpoint/bucket/clave: las aceptan B2 y MinIO
        var config = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        this.s3 = S3Client.builder()
                .httpClient(UrlConnectionHttpClient.create())
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .serviceConfiguration(config)
                // Not every provider accepts the automatic CRC32 checksums of the new SDK
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        // Signed links are opened by the contact's browser: in local Docker the backend sees
        // "minio:9000" but the browser sees "localhost:9000"
        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint.isBlank() ? endpoint : publicEndpoint))
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .serviceConfiguration(config)
                .build();
        this.bucket = bucket;
        if (createBucket) createBucketIfMissing();
    }

    @Override
    public StoredFile save(IncomingFile file) {
        String key = UUID.randomUUID().toString();
        try {
            String sha256 = fingerprint(file);
            // The SDK may read the content more than once (signing, retries): it gets a way to
            // open it again, which the multipart upload allows (kept in memory or on disk)
            ContentStreamProvider content = () -> file.content().get();
            s3.putObject(b -> b.bucket(bucket).key(PREFIX + key).contentType(file.contentType()),
                    RequestBody.fromContentProvider(content, file.size(), file.contentType()));
            return new StoredFile(key, sha256, file.size());
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("No se pudo leer la evidencia", e);
        } catch (S3Exception e) {
            log.error("No se pudo subir la evidencia al almacenamiento: {}", e.awsErrorDetails().errorMessage());
            throw new IllegalStateException("No se pudo guardar la evidencia");
        }
    }

    private static String fingerprint(IncomingFile file) throws IOException {
        try (InputStream in = file.content().get();
             DigestInputStream digest = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            digest.transferTo(OutputStream.nullOutputStream());
            return HexFormat.of().formatHex(digest.getMessageDigest().digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Resource load(String key) {
        return new BucketObject(validate(key));
    }

    @Override
    public void delete(String key) {
        if (key == null) return;
        try {
            s3.deleteObject(b -> b.bucket(bucket).key(PREFIX + validate(key)));
        } catch (S3Exception e) {
            // If the file cannot be deleted, that must not prevent deleting the record
            log.warn("No se pudo borrar la evidencia {} del almacenamiento: {}", key, e.getMessage());
        }
    }

    @Override
    public Optional<URI> temporaryLink(String key, String contentType, Duration validity) {
        String objectKey = PREFIX + validate(key);
        var signed = presigner.presignGetObject(p -> p
                .signatureDuration(validity)
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .responseContentType(contentType)
                        .responseContentDisposition("inline")
                        .build()));
        return Optional.of(URI.create(signed.url().toString()));
    }

    // The key is always a UUID generated here; it is validated anyway so no other objects are touched
    private static String validate(String key) {
        return UUID.fromString(key).toString();
    }

    private void createBucketIfMissing() {
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

    /** Bucket object as a Resource: Spring serves it with Range support (seeking in videos). */
    private final class BucketObject extends AbstractResource {
        private final String key;
        private HeadObjectResponse head;

        private BucketObject(String key) {
            this.key = key;
        }

        private Optional<HeadObjectResponse> head() {
            if (head == null) {
                try {
                    head = s3.headObject(b -> b.bucket(bucket).key(PREFIX + key));
                } catch (NoSuchKeyException e) {
                    return Optional.empty();
                } catch (S3Exception e) {
                    if (e.statusCode() == 404) return Optional.empty();
                    throw e;
                }
            }
            return Optional.of(head);
        }

        @Override
        public boolean exists() {
            return head().isPresent();
        }

        @Override
        public long contentLength() throws IOException {
            return head().map(HeadObjectResponse::contentLength)
                    .orElseThrow(() -> new IOException("La evidencia no existe en el almacenamiento"));
        }

        @Override
        public InputStream getInputStream() {
            return s3.getObject(b -> b.bucket(bucket).key(PREFIX + key));
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
