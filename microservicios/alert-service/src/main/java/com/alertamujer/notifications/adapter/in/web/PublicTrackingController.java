package com.alertamujer.notifications.adapter.in.web;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase.TrackingView;
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

    private final SeguimientoPublicoUseCase seguimiento;

    public PublicTrackingController(SeguimientoPublicoUseCase seguimiento) {
        this.seguimiento = seguimiento;
    }

    /** v: identificador anónimo del navegador o la app que mira (para contar las vistas). */
    @GetMapping("/api/public/tracking/{codigo}")
    public ResponseEntity<TrackingView> consultar(@PathVariable String codigo,
                                                  @RequestParam(name = "v", required = false) String visitante) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(seguimiento.consultar(codigo, visitante, LocalDateTime.now()));
    }

    /** Chat de la alerta para el contacto. after: id del último mensaje que ya tiene (0 = todos). */
    @GetMapping("/api/public/tracking/{codigo}/messages")
    public ResponseEntity<List<AlertMessageView>> mensajes(@PathVariable String codigo,
                                                           @RequestParam(defaultValue = "0") Long after) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(seguimiento.mensajes(codigo, after, LocalDateTime.now()));
    }

    @PostMapping("/api/public/tracking/{codigo}/messages")
    public ResponseEntity<AlertMessageView> enviarMensaje(@PathVariable String codigo, @RequestBody MensajeContacto mensaje) {
        return ResponseEntity.ok(seguimiento.enviarMensaje(codigo, mensaje.name(), mensaje.body(), LocalDateTime.now()));
    }

    /** name: cómo se presenta el contacto (el enlace no dice quién es). */
    public record MensajeContacto(String name, String body) {}

    /**
     * Video o audio de la alerta para el contacto. Con almacenamiento S3 redirige a un enlace firmado
     * que vence en minutos; en disco entrega el archivo (con Range, para adelantar).
     */
    @GetMapping("/api/public/tracking/{codigo}/evidence/{id}")
    public ResponseEntity<Resource> evidencia(@PathVariable String codigo, @PathVariable Long id) {
        EvidenceApi.EvidenceFile archivo = seguimiento.evidencia(codigo, id, LocalDateTime.now());
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
