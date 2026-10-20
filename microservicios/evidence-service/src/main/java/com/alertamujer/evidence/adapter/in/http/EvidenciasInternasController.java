package com.alertamujer.evidence.adapter.in.http;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador de entrada para Alert Service (página de seguimiento de los contactos). Protegido por la
 * clave interna (ClaveInternaFilter). Entrega los datos y el enlace firmado aparte del contenido.
 */
@RestController
@RequestMapping("/internal/evidence/alerts/{alertId}")
class EvidenciasInternasController {

    private final EvidenceApi evidencias;

    EvidenciasInternasController(EvidenceApi evidencias) {
        this.evidencias = evidencias;
    }

    @GetMapping
    List<EvidenceApi.EvidenceSummary> deLaAlerta(@PathVariable Long alertId) {
        return evidencias.evidenciasDeAlerta(alertId);
    }

    @GetMapping("/evidences/{id}")
    ResponseEntity<DatosArchivo> datos(@PathVariable Long alertId, @PathVariable Long id) {
        return evidencias.archivo(alertId, id)
                .map(a -> ResponseEntity.ok(new DatosArchivo(a.contentType(), a.fileName(),
                        a.enlaceTemporal().map(Object::toString).orElse(null))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/evidences/{id}/content")
    ResponseEntity<Resource> contenido(@PathVariable Long alertId, @PathVariable Long id) {
        return evidencias.archivo(alertId, id)
                .map((EvidenceFile a) -> ResponseEntity.ok().contentType(MediaType.parseMediaType(a.contentType())).body(a.resource()))
                .orElse(ResponseEntity.notFound().build());
    }

    record DatosArchivo(String contentType, String fileName, String enlaceTemporal) {}
}
