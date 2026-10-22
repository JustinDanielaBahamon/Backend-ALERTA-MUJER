package com.alertamujer.resources.adapter.in.web;

import com.alertamujer.resources.adapter.in.web.dto.EmergencyResourceRequest;
import com.alertamujer.resources.adapter.in.web.dto.EmergencyResourceResponse;
import com.alertamujer.resources.application.port.in.RecursosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de los recursos de ayuda. */
@RestController
@RequestMapping("/api/resources")
public class EmergencyResourceController {

    private final RecursosUseCase recursos;

    public EmergencyResourceController(RecursosUseCase recursos) {
        this.recursos = recursos;
    }

    @GetMapping
    public ResponseEntity<List<EmergencyResourceResponse>> getAllResources() {
        return ResponseEntity.ok(recursos.all().stream().map(EmergencyResourceResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyResourceResponse> getResourceById(@PathVariable Long id) {
        return recursos.one(id).map(r -> ResponseEntity.ok(EmergencyResourceResponse.of(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<EmergencyResourceResponse>> getResourcesByCity(@PathVariable String city) {
        return ResponseEntity.ok(recursos.deCiudad(city).stream().map(EmergencyResourceResponse::of).toList());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EmergencyResourceResponse>> getResourcesByType(@PathVariable String type) {
        return ResponseEntity.ok(recursos.deTipo(type).stream().map(EmergencyResourceResponse::of).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResourceResponse> createResource(@RequestBody EmergencyResourceRequest request) {
        return ResponseEntity.ok(EmergencyResourceResponse.of(recursos.create(request.toData())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResourceResponse> updateResource(@PathVariable Long id, @RequestBody EmergencyResourceRequest request) {
        return ResponseEntity.ok(EmergencyResourceResponse.of(recursos.replace(id, request.toData())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        recursos.delete(id);
        return ResponseEntity.noContent().build();
    }
}
