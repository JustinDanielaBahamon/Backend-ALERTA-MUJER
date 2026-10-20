package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.LocationLogRequest;
import com.alertamujer.alerts.adapter.in.web.dto.LocationLogResponse;
import com.alertamujer.alerts.application.port.in.PedirUbicacionUseCase;
import com.alertamujer.alerts.application.port.in.UbicacionesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Ubicaciones que reporta la app móvil. Son datos muy sensibles: cada usuaria solo ve las suyas
 * (la administración puede consultar las de cualquier usuaria).
 */
@RestController
@RequestMapping("/api/location-logs")
public class LocationLogController {

    private final UbicacionesUseCase ubicaciones;
    private final PedirUbicacionUseCase pedirUbicacion;

    public LocationLogController(UbicacionesUseCase ubicaciones, PedirUbicacionUseCase pedirUbicacion) {
        this.ubicaciones = ubicaciones;
        this.pedirUbicacion = pedirUbicacion;
    }

    @PostMapping
    public ResponseEntity<LocationLogResponse> saveLocation(@RequestBody LocationLogRequest request,
                                                            @AuthenticationPrincipal Long currentUserId) {
        // 202: llegó bien pero no se guarda (es de un celular que no es el principal)
        return ubicaciones.guardar(request.aNueva(), currentUserId)
                .map(l -> ResponseEntity.ok(LocationLogResponse.of(l)))
                .orElse(ResponseEntity.accepted().build());
    }

    @GetMapping("/me")
    public ResponseEntity<List<LocationLogResponse>> getMine(@AuthenticationPrincipal Long currentUserId,
                                                             @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ubicaciones.recientes(currentUserId, limit).stream().map(LocationLogResponse::of).toList());
    }

    /** Pide a los celulares de la usuaria que envíen su ubicación actual (push silencioso). */
    @PostMapping("/me/request")
    public ResponseEntity<Map<String, Object>> requestFromPhone(@AuthenticationPrincipal Long currentUserId) {
        try {
            var r = pedirUbicacion.pedir(currentUserId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("celulares", r.celulares(), "solicitadoEn", r.solicitadoEn().toString()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    /** Última ubicación enviada por el celular de la usuaria (204 si nunca ha enviado). */
    @GetMapping("/me/latest")
    public ResponseEntity<LocationLogResponse> getMyLatest(@AuthenticationPrincipal Long currentUserId) {
        return ubicaciones.ultima(currentUserId)
                .map(l -> ResponseEntity.ok(LocationLogResponse.of(l)))
                .orElse(ResponseEntity.noContent().build());
    }

    // userId = users.id. Solo la dueña: la administración solo ve el recorrido de alertas (auditoría S2).
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal")
    public ResponseEntity<List<LocationLogResponse>> getByUser(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ubicaciones.recientes(userId, limit).stream().map(LocationLogResponse::of).toList());
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<LocationLogResponse>> getByAlert(@PathVariable Long alertId,
                                                                @AuthenticationPrincipal Long currentUserId,
                                                                Authentication authentication) {
        return ResponseEntity.ok(ubicaciones.deLaAlerta(alertId, currentUserId, Autorizacion.esAdmin(authentication)).stream()
                .map(LocationLogResponse::of).toList());
    }
}
