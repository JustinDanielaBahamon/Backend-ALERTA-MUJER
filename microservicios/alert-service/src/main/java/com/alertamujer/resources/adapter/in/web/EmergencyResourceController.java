package com.alertamujer.resources.adapter.in.web;

import com.alertamujer.resources.adapter.in.web.dto.EmergencyResourceRequest;
import com.alertamujer.resources.adapter.in.web.dto.EmergencyResourceResponse;
import com.alertamujer.resources.application.port.in.ResourcesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de los recursos de ayuda. */
@RestController
@RequestMapping("/api/resources")
public class EmergencyResourceController {

    private final ResourcesUseCase resources;

    public EmergencyResourceController(ResourcesUseCase resources) {
        this.resources = resources;
    }

    @GetMapping
    public ResponseEntity<List<EmergencyResourceResponse>> getAllResources() {
        return ResponseEntity.ok(resources.all().stream().map(EmergencyResourceResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyResourceResponse> getResourceById(@PathVariable Long id) {
        return resources.one(id).map(r -> ResponseEntity.ok(EmergencyResourceResponse.of(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<EmergencyResourceResponse>> getResourcesByCity(@PathVariable String city) {
        return ResponseEntity.ok(resources.ofCity(city).stream().map(EmergencyResourceResponse::of).toList());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EmergencyResourceResponse>> getResourcesByType(@PathVariable String type) {
        return ResponseEntity.ok(resources.ofType(type).stream().map(EmergencyResourceResponse::of).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResourceResponse> createResource(@RequestBody EmergencyResourceRequest request) {
        return ResponseEntity.ok(EmergencyResourceResponse.of(resources.create(request.toData())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResourceResponse> updateResource(@PathVariable Long id, @RequestBody EmergencyResourceRequest request) {
        return ResponseEntity.ok(EmergencyResourceResponse.of(resources.replace(id, request.toData())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resources.delete(id);
        return ResponseEntity.noContent().build();
    }
}
