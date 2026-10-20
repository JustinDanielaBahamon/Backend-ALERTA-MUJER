package com.alertamujer.resources.infrastructure.controller;

import com.alertamujer.resources.application.service.EmergencyResourceService;
import com.alertamujer.resources.domain.model.EmergencyResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class EmergencyResourceController {

    private final EmergencyResourceService emergencyResourceService;

    public EmergencyResourceController(EmergencyResourceService emergencyResourceService) {
        this.emergencyResourceService = emergencyResourceService;
    }

    @GetMapping
    public ResponseEntity<List<EmergencyResource>> getAllResources() {
        return ResponseEntity.ok(emergencyResourceService.getAllResources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyResource> getResourceById(@PathVariable Long id) {
        return emergencyResourceService.getResourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<EmergencyResource>> getResourcesByCity(@PathVariable String city) {
        return ResponseEntity.ok(emergencyResourceService.getResourcesByCity(city));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EmergencyResource>> getResourcesByType(@PathVariable String type) {
        return ResponseEntity.ok(emergencyResourceService.getResourcesByType(type));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResource> createResource(@RequestBody EmergencyResource resource) {
        return ResponseEntity.ok(emergencyResourceService.createResource(resource));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmergencyResource> updateResource(@PathVariable Long id, @RequestBody EmergencyResource resource) {
        return ResponseEntity.ok(emergencyResourceService.updateResource(id, resource));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        emergencyResourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
