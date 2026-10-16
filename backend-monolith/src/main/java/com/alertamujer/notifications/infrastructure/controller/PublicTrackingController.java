package com.alertamujer.notifications.infrastructure.controller;

import com.alertamujer.notifications.application.TrackingLinkService;
import org.springframework.http.CacheControl;
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
}
