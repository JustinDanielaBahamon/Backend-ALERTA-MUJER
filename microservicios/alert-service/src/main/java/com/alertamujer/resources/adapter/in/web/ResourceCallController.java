package com.alertamujer.resources.adapter.in.web;

import com.alertamujer.resources.adapter.in.web.dto.ResourceCallRequest;
import com.alertamujer.resources.adapter.in.web.dto.ResourceCallResponse;
import com.alertamujer.resources.application.port.in.LlamadasUseCase;
import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de las llamadas a los recursos. */
@RestController
@RequestMapping("/api/resource-calls")
public class ResourceCallController {

    private final LlamadasUseCase llamadas;

    public ResourceCallController(LlamadasUseCase llamadas) {
        this.llamadas = llamadas;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCallResponse>> getAllCalls() {
        return ResponseEntity.ok(vistas(llamadas.todas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceCallResponse> getCallById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return llamadas.una(id, currentUserId).map(c -> ResponseEntity.ok(ResourceCallResponse.of(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // userId = users.id. Solo la dueña o la administración.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<List<ResourceCallResponse>> getCallsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(vistas(llamadas.mias(userId)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResourceCallResponse>> getMyCalls(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(vistas(llamadas.mias(currentUserId)));
    }

    @GetMapping("/resource/{emergencyResourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCallResponse>> getCallsByResource(@PathVariable Long emergencyResourceId) {
        return ResponseEntity.ok(vistas(llamadas.deRecurso(emergencyResourceId)));
    }

    @GetMapping("/alert/{alertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCallResponse>> getCallsByAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(vistas(llamadas.deAlerta(alertId)));
    }

    @PostMapping
    public ResponseEntity<ResourceCallResponse> createCall(@RequestBody ResourceCallRequest r, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ResourceCallResponse.of(llamadas.registrar(r.emergencyResourceId(), r.alertId(), r.deviceId(),
                r.telephoneDialed(), r.status(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceCallResponse> updateCall(@PathVariable Long id, @RequestBody ResourceCallRequest r,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ResourceCallResponse.of(llamadas.actualizar(id, r.status(), r.endedAt(), r.durationSeconds(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCall(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        llamadas.eliminar(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    private static List<ResourceCallResponse> vistas(List<ResourceCall> lista) {
        return lista.stream().map(ResourceCallResponse::of).toList();
    }
}
