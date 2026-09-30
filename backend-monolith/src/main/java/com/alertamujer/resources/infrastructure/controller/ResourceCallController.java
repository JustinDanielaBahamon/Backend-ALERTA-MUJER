package com.alertamujer.resources.infrastructure.controller;

import com.alertamujer.resources.application.service.ResourceCallService;
import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource-calls")
public class ResourceCallController {

    private final ResourceCallService resourceCallService;

    public ResourceCallController(ResourceCallService resourceCallService) {
        this.resourceCallService = resourceCallService;
    }

    @GetMapping
    public ResponseEntity<List<ResourceCall>> getAllCalls() {
        return ResponseEntity.ok(resourceCallService.getAllCalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceCall> getCallById(@PathVariable Long id) {
        return resourceCallService.getCallById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<ResourceCall>> getCallsByUser(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(resourceCallService.getCallsByUserProfile(userProfileId));
    }

    @GetMapping("/resource/{emergencyResourceId}")
    public ResponseEntity<List<ResourceCall>> getCallsByResource(@PathVariable Long emergencyResourceId) {
        return ResponseEntity.ok(resourceCallService.getCallsByResource(emergencyResourceId));
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<ResourceCall>> getCallsByAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(resourceCallService.getCallsByAlert(alertId));
    }

    @PostMapping
    public ResponseEntity<ResourceCall> createCall(@RequestBody ResourceCall call) {
        return ResponseEntity.ok(resourceCallService.createCall(call));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceCall> updateCall(@PathVariable Long id, @RequestBody ResourceCall call) {
        return ResponseEntity.ok(resourceCallService.updateCall(id, call));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCall(@PathVariable Long id) {
        resourceCallService.deleteCall(id);
        return ResponseEntity.noContent().build();
    }
}
