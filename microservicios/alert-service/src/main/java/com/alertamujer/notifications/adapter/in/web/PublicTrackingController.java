package com.alertamujer.notifications.adapter.in.web;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase.TrackingView;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** Adaptador de entrada REST: seguimiento público de una alerta (lo abre el contacto desde el correo, sin iniciar sesión). */
@RestController
public class PublicTrackingController {

    private final PublicTrackingUseCase tracking;

    public PublicTrackingController(PublicTrackingUseCase tracking) {
        this.tracking = tracking;
    }

    /** v: identificador anónimo del navegador o la app que mira (para contar las vistas). */
    @GetMapping("/api/public/tracking/{code}")
    public ResponseEntity<TrackingView> query(@PathVariable String code,
                                                  @RequestParam(name = "v", required = false) String visitor) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(tracking.query(code, visitor, LocalDateTime.now()));
    }

    /** Chat de la alerta para el contacto. after: id del último mensaje que ya tiene (0 = todos). */
    @GetMapping("/api/public/tracking/{code}/messages")
    public ResponseEntity<List<AlertMessageView>> messages(@PathVariable String code,
                                                           @RequestParam(defaultValue = "0") Long after) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(tracking.messages(code, after, LocalDateTime.now()));
    }

    @PostMapping("/api/public/tracking/{code}/messages")
    public ResponseEntity<AlertMessageView> sendMessage(@PathVariable String code, @RequestBody ContactMessage message) {
        return ResponseEntity.ok(tracking.sendMessage(code, message.name(), message.body(), LocalDateTime.now()));
    }

    /** name: cómo se presenta el contacto (el enlace no dice quién es). */
    public record ContactMessage(String name, String body) {}

    /**
     * Video o audio de la alerta para el contacto. Con almacenamiento S3 redirige a un enlace firmado
     * que vence en minutos; en disco entrega el archivo (con Range, para adelantar).
     */
    @GetMapping("/api/public/tracking/{code}/evidence/{id}")
    public ResponseEntity<Resource> evidence(@PathVariable String code, @PathVariable Long id) {
        EvidenceApi.EvidenceFile file = tracking.evidence(code, id, LocalDateTime.now());
        if (file.temporaryLink().isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(file.temporaryLink().get())
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .cacheControl(CacheControl.noStore())
                .body(file.resource());
    }
}
