package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.FrequentLocationRequest;
import com.alertamujer.alerts.adapter.in.web.dto.FrequentLocationResponse;
import com.alertamujer.alerts.application.port.in.LugaresFrecuentesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de los lugares frecuentes. */
@RestController
@RequestMapping("/api/frequent-locations")
public class FrequentLocationController {

    private final LugaresFrecuentesUseCase lugares;

    public FrequentLocationController(LugaresFrecuentesUseCase lugares) {
        this.lugares = lugares;
    }

    // userId = users.id (lo que envían web y móvil). Solo la dueña (auditoría S2).
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal")
    public ResponseEntity<List<FrequentLocationResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(lugares.activosDe(userId).stream().map(FrequentLocationResponse::of).toList());
    }

    @GetMapping("/me")
    public ResponseEntity<List<FrequentLocationResponse>> getMyLocations(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(lugares.activosDe(currentUserId).stream().map(FrequentLocationResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FrequentLocationResponse> getById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(FrequentLocationResponse.of(lugares.uno(id, currentUserId)));
    }

    @PostMapping
    public ResponseEntity<FrequentLocationResponse> create(@RequestBody FrequentLocationRequest request,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(FrequentLocationResponse.of(lugares.create(request.aDatos(), currentUserId)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<FrequentLocationResponse> update(@PathVariable Long id, @RequestBody FrequentLocationRequest request,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(FrequentLocationResponse.of(lugares.actualizar(id, request.aDatos(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        lugares.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
