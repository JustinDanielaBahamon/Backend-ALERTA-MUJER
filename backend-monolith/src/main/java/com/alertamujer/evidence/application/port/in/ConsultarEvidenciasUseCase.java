package com.alertamujer.evidence.application.port.in;

import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.core.io.Resource;

import java.util.List;

/** Casos de uso de lectura de la usuaria: sus evidencias, una evidencia y su archivo. */
public interface ConsultarEvidenciasUseCase {

    List<EvidenciaConAlerta> deLaUsuaria(Long userId);

    Evidence una(Long id, Long userId);

    /** Archivo de la evidencia; si admin es true no se verifica la dueña. */
    ArchivoEvidencia archivo(Long id, Long userId, boolean admin);

    record ArchivoEvidencia(Evidence evidencia, Resource contenido) {}
}
