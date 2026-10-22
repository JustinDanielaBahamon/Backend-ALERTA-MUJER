package com.alertamujer.evidence.api;

import org.springframework.core.io.Resource;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Lo que otros módulos pueden consultar de las evidencias (p. ej. el seguimiento de los contactos). */
public interface EvidenceApi {

    /** Evidencias con archivo de una alerta, de la más antigua a la más reciente. */
    List<EvidenceSummary> alertEvidence(Long alertId);

    /** Archivo de una evidencia, solo si pertenece a esa alerta. */
    Optional<EvidenceFile> file(Long alertId, Long evidenceId);

    record EvidenceSummary(Long id, String mediaType, String contentType, Long sizeBytes, LocalDateTime createdAt) {}

    /** enlaceTemporal: abrirlo directo del almacenamiento (S3) si está disponible; si no, entregar resource. */
    record EvidenceFile(String contentType, String fileName, Resource resource, Optional<URI> temporaryLink) {}
}
