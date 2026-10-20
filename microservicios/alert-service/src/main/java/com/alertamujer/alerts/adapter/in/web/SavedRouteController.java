package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.SavedRouteRequest;
import com.alertamujer.alerts.adapter.in.web.dto.SavedRouteResponse;
import com.alertamujer.alerts.adapter.in.web.dto.SavedRouteUpdateRequest;
import com.alertamujer.alerts.application.port.in.RecorridosUseCase;
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

    private final RecorridosUseCase recorridos;

    public SavedRouteController(RecorridosUseCase recorridos) {
        this.recorridos = recorridos;
    }

    @GetMapping("/me")
    public ResponseEntity<List<SavedRouteResponse>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(recorridos.mios(currentUserId).stream().map(SavedRouteResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<SavedRouteResponse> create(@RequestBody SavedRouteRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(SavedRouteResponse.of(recorridos.crear(request.aNuevo(), currentUserId)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SavedRouteResponse> update(@PathVariable Long id, @RequestBody SavedRouteUpdateRequest changes,
                                                     @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(SavedRouteResponse.of(
                recorridos.actualizar(id, changes.name(), changes.isStarred(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        recorridos.eliminar(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
