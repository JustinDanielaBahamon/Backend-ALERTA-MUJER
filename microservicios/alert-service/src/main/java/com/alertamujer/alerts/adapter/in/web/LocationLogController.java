package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.LocationLogRequest;
import com.alertamujer.alerts.adapter.in.web.dto.LocationLogResponse;
import com.alertamujer.alerts.application.port.in.RequestLocationUseCase;
import com.alertamujer.alerts.application.port.in.LocationsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Locations the mobile app reports. They are very sensitive data: each user only sees her own
 * (the administration can query any user's).
 */
@RestController
@RequestMapping("/api/location-logs")
public class LocationLogController {

    private final LocationsUseCase locations;
    private final RequestLocationUseCase requestLocation;

    public LocationLogController(LocationsUseCase locations, RequestLocationUseCase requestLocation) {
        this.locations = locations;
        this.requestLocation = requestLocation;
    }

    @PostMapping
    public ResponseEntity<LocationLogResponse> saveLocation(@RequestBody LocationLogRequest request,
                                                            @AuthenticationPrincipal Long currentUserId) {
        // 202: arrived fine but is not stored (it is from a phone that is not the primary one)
        return locations.save(request.toNew(), currentUserId)
                .map(l -> ResponseEntity.ok(LocationLogResponse.of(l)))
                .orElse(ResponseEntity.accepted().build());
    }

    @GetMapping("/me")
    public ResponseEntity<List<LocationLogResponse>> getMine(@AuthenticationPrincipal Long currentUserId,
                                                             @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(locations.recent(currentUserId, limit).stream().map(LocationLogResponse::of).toList());
    }

    /** Asks the user's phones to send their current location (silent push). */
    @PostMapping("/me/request")
    public ResponseEntity<Map<String, Object>> requestFromPhone(@AuthenticationPrincipal Long currentUserId) {
        try {
            var r = requestLocation.perform(currentUserId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("phones", r.phones(), "requestedAt", r.requestedAt().toString()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    /** Last location sent by the user's phone (204 if it never sent one). */
    @GetMapping("/me/latest")
    public ResponseEntity<LocationLogResponse> getMyLatest(@AuthenticationPrincipal Long currentUserId) {
        return locations.latest(currentUserId)
                .map(l -> ResponseEntity.ok(LocationLogResponse.of(l)))
                .orElse(ResponseEntity.noContent().build());
    }

    // userId = users.id. Owner only: the administration only sees alert routes (audit S2).
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal")
    public ResponseEntity<List<LocationLogResponse>> getByUser(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(locations.recent(userId, limit).stream().map(LocationLogResponse::of).toList());
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<LocationLogResponse>> getByAlert(@PathVariable Long alertId,
                                                                @AuthenticationPrincipal Long currentUserId,
                                                                Authentication authentication) {
        return ResponseEntity.ok(locations.ofAlert(alertId, currentUserId, Authorization.esAdmin(authentication)).stream()
                .map(LocationLogResponse::of).toList());
    }
}
