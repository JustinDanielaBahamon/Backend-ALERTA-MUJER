package com.alertamujer.notifications.infrastructure.controller;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.notifications.application.TrackingLinkService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** Seguimiento público de una alerta (lo abre el contacto desde el correo, sin iniciar sesión). */
@RestController
public class PublicTrackingController {

    private final TrackingLinkService trackingLinkService;

    public PublicTrackingController(TrackingLinkService trackingLinkService) {
        this.trackingLinkService = trackingLinkService;
    }

    @GetMapping("/api/public/tracking/{codigo}")
    public ResponseEntity<TrackingLinkService.TrackingView> consultar(@PathVariable String codigo) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(trackingLinkService.consultar(codigo, LocalDateTime.now()));
    }

    /**
     * Video o audio de la alerta para el contacto. Con almacenamiento S3 redirige a un enlace firmado
     * que vence en minutos; en disco entrega el archivo (con Range, para adelantar).
     */
    @GetMapping("/api/public/tracking/{codigo}/evidence/{id}")
    public ResponseEntity<Resource> evidencia(@PathVariable String codigo, @PathVariable Long id) {
        EvidenceApi.EvidenceFile archivo = trackingLinkService.evidencia(codigo, id, LocalDateTime.now());
        if (archivo.enlaceTemporal().isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(archivo.enlaceTemporal().get())
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .cacheControl(CacheControl.noStore())
                .body(archivo.resource());
    }
}
