package com.alertamujer.evidence.adapter.in.modulo;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de entrada para otros módulos (el seguimiento de los contactos). Implementa la API
 * pública del módulo; como microservicio, estos mismos datos se servirían por HTTP.
 */
@Component
class EvidenceApiAdapter implements EvidenceApi {

    /** Lo justo para reproducir el archivo; la página de seguimiento pide uno nuevo cada vez. */
    static final Duration VIGENCIA_ENLACE = Duration.ofMinutes(10);

    private final EvidenciaRepositorio repositorio;
    private final AlmacenamientoEvidencias almacenamiento;

    EvidenceApiAdapter(EvidenciaRepositorio repositorio, AlmacenamientoEvidencias almacenamiento) {
        this.repositorio = repositorio;
        this.almacenamiento = almacenamiento;
    }

    @Override
    public List<EvidenceSummary> evidenciasDeAlerta(Long alertId) {
        return repositorio.deLaAlerta(alertId).stream()
                .filter(Evidence::tieneArchivo)
                .map(e -> new EvidenceSummary(e.getId(), e.getMediaType(), e.getContentType(), e.getSizeBytes(), e.getCreatedAt()))
                .toList();
    }

    @Override
    public Optional<EvidenceFile> archivo(Long alertId, Long evidenceId) {
        return repositorio.buscar(evidenceId)
                .filter(e -> e.getAlertId().equals(alertId) && e.tieneArchivo())
                .map(e -> new EvidenceFile(e.getContentType(), e.getFileName(), almacenamiento.cargar(e.getStorageKey()),
                        almacenamiento.enlaceTemporal(e.getStorageKey(), e.getContentType(), VIGENCIA_ENLACE)));
    }
}
