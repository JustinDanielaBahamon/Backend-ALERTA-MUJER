package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.service.LocationLogService;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.infrastructure.controller.dto.LocationLogRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ubicaciones que reporta la app móvil. Son datos muy sensibles: cada usuaria solo ve las suyas
 * (los admins pueden consultar las de cualquier usuaria).
 */
@RestController
@RequestMapping("/api/location-logs")
public class LocationLogController {

    private final LocationLogService locationLogService;

    public LocationLogController(LocationLogService locationLogService) {
        this.locationLogService = locationLogService;
    }

    @PostMapping
    public ResponseEntity<LocationLog> saveLocation(@RequestBody LocationLogRequest request,
                                                    @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(locationLogService.saveLocation(request, currentUserId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LocationLog>> getMine(@AuthenticationPrincipal Long currentUserId,
                                                     @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(locationLogService.getRecentForUser(currentUserId, limit));
    }

    /** Última ubicación enviada por el celular de la usuaria (204 si nunca ha enviado). */
    @GetMapping("/me/latest")
    public ResponseEntity<LocationLog> getMyLatest(@AuthenticationPrincipal Long currentUserId) {
        return locationLogService.getLatestForUser(currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // userId = users.id. Antes cualquier usuaria con sesión podía leer el GPS de otra.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<List<LocationLog>> getByUser(@PathVariable Long userId,
                                                       @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(locationLogService.getRecentForUser(userId, limit));
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<LocationLog>> getByAlert(@PathVariable Long alertId,
                                                        @AuthenticationPrincipal Long currentUserId,
                                                        Authentication authentication) {
        return ResponseEntity.ok(locationLogService.getByAlert(alertId, currentUserId, isAdmin(authentication)));
    }

    static boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
