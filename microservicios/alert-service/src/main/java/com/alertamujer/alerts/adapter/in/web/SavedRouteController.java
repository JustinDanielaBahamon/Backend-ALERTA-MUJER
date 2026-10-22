package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.SavedRouteRequest;
import com.alertamujer.alerts.adapter.in.web.dto.SavedRouteResponse;
import com.alertamujer.alerts.adapter.in.web.dto.SavedRouteUpdateRequest;
import com.alertamujer.alerts.application.port.in.RoutesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Saved routes of the authenticated user (shared between web and mobile).
 * Always works on the token owner: there is no way to read someone else's routes.
 */
@RestController
@RequestMapping("/api/saved-routes")
public class SavedRouteController {

    private final RoutesUseCase routes;

    public SavedRouteController(RoutesUseCase routes) {
        this.routes = routes;
    }

    @GetMapping("/me")
    public ResponseEntity<List<SavedRouteResponse>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(routes.mine(currentUserId).stream().map(SavedRouteResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<SavedRouteResponse> create(@RequestBody SavedRouteRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(SavedRouteResponse.of(routes.create(request.toNew(), currentUserId)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SavedRouteResponse> update(@PathVariable Long id, @RequestBody SavedRouteUpdateRequest changes,
                                                     @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(SavedRouteResponse.of(
                routes.update(id, changes.name(), changes.isStarred(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        routes.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
