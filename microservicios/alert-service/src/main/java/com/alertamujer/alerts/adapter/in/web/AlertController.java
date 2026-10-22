package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.AlertRequest;
import com.alertamujer.alerts.adapter.in.web.dto.AlertResponse;
import com.alertamujer.alerts.adapter.in.web.dto.LocationLogResponse;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.application.port.in.AdministrarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.ChatAlertaUseCase;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase.ContactoNotificado;
import com.alertamujer.alerts.application.port.in.GestionarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.UbicacionesUseCase;
import com.alertamujer.alerts.domain.model.AlertMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Adaptador de entrada REST de las alertas. Solo traduce HTTP ↔ casos de uso. */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final GestionarAlertasUseCase alerts;
    private final AdministrarAlertasUseCase administracion;
    private final DestinatariosAlertaUseCase destinatarios;
    private final ChatAlertaUseCase chat;
    private final UbicacionesUseCase ubicaciones;

    public AlertController(GestionarAlertasUseCase alerts, AdministrarAlertasUseCase administracion,
                           DestinatariosAlertaUseCase destinatarios, ChatAlertaUseCase chat,
                           UbicacionesUseCase ubicaciones) {
        this.alerts = alerts;
        this.administracion = administracion;
        this.destinatarios = destinatarios;
        this.chat = chat;
        this.ubicaciones = ubicaciones;
    }

    /** Recorrido GPS de una alerta (lo usa el seguimiento de alertas del móvil). */
    @GetMapping("/{id}/locations")
    public ResponseEntity<List<LocationLogResponse>> getAlertLocations(@PathVariable Long id,
                                                                       @AuthenticationPrincipal Long currentUserId,
                                                                       Authentication authentication) {
        return ResponseEntity.ok(ubicaciones.ofAlert(id, currentUserId, Autorizacion.esAdmin(authentication)).stream()
                .map(LocationLogResponse::of).toList());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminAlertView>> getAllAlerts() {
        return ResponseEntity.ok(administracion.listAll());
    }

    /** Detalle del panel: última ubicación y estado de envío a cada contacto (sin sus datos). */
    @GetMapping("/{id}/admin-detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministrarAlertasUseCase.DetalleAdmin> getAdminDetail(@PathVariable Long id) {
        return ResponseEntity.ok(administracion.detalle(id));
    }

    /** Cambia el estado de una alerta de cualquier usuaria, por ejemplo a "resolved" (atendida). */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponse> changeAlertStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(AlertResponse.of(administracion.cambiarEstado(id, body.get("status"))));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AlertResponse>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alerts.mias(currentUserId).stream().map(AlertResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return alerts.una(id, currentUserId)
                .map(a -> ResponseEntity.ok(AlertResponse.of(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByUser(@PathVariable Long userProfileId,
                                                               @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alerts.deUsuaria(userProfileId, currentUserId).stream().map(AlertResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@RequestBody AlertRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(AlertResponse.of(alerts.activar(request.aNueva(), currentUserId)));
    }

    /** Contactos avisados en una alerta y el estado de cada canal (solo la dueña de la alerta). */
    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<ContactoNotificado>> getAlertContacts(@PathVariable Long id,
                                                                     @AuthenticationPrincipal Long currentUserId) {
        return alerts.una(id, currentUserId)
                .map(a -> ResponseEntity.ok(destinatarios.contactosDe(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Chat de la alerta (solo la dueña). after: id del último mensaje que ya tiene (0 = todos). */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AlertMessageView>> getMessages(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "0") Long after,
                                                              @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(chat.mensajesDeLaDuena(id, currentUserId, after).stream().map(AlertController::view).toList());
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AlertMessageView> sendMessage(@PathVariable Long id, @RequestBody MensajeRequest request,
                                                        @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(view(chat.enviarDeLaDuena(id, currentUserId, request.body())));
    }

    public record MensajeRequest(String body) {}

    /** Conservar las evidencias de la alerta (no se borran a los 60 días). Body: {"keep": true}. */
    @PatchMapping("/{id}/keep-evidence")
    public ResponseEntity<AlertResponse> keepEvidence(@PathVariable Long id, @RequestBody Map<String, Boolean> body,
                                                      @AuthenticationPrincipal Long currentUserId) {
        boolean conservar = body != null && Boolean.TRUE.equals(body.get("keep"));
        return ResponseEntity.ok(AlertResponse.of(alerts.conservarEvidencias(id, conservar, currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertResponse> updateAlert(@PathVariable Long id, @RequestBody AlertRequest request,
                                                     @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(AlertResponse.of(alerts.actualizar(id, request.aCambios(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        alerts.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    private static AlertMessageView view(AlertMessage m) {
        return new AlertMessageView(m.getId(), m.getSenderType(), m.getSenderName(), m.getBody(), m.getCreatedAt());
    }
}
