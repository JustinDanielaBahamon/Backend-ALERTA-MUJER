package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.service.SavedRouteService;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.alertamujer.alerts.infrastructure.controller.dto.SavedRouteRequest;
import com.alertamujer.alerts.infrastructure.controller.dto.SavedRouteUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Recorridos guardados de la usuaria autenticada (compartidos entre web y móvil).
 * Siempre opera sobre la dueña del token: no hay forma de leer recorridos ajenos.
 */
@RestController
@RequestMapping("/api/saved-routes")
public class SavedRouteController {

    private final SavedRouteService savedRouteService;

    public SavedRouteController(SavedRouteService savedRouteService) {
        this.savedRouteService = savedRouteService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<SavedRoute>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(savedRouteService.getMine(currentUserId));
    }

    @PostMapping
    public ResponseEntity<SavedRoute> create(@RequestBody SavedRouteRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(savedRouteService.create(request, currentUserId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SavedRoute> update(@PathVariable Long id, @RequestBody SavedRouteUpdateRequest changes,
                                             @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(savedRouteService.update(id, changes, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        savedRouteService.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
