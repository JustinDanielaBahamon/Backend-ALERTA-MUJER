package com.alertamujer.resources.infrastructure.controller;

import com.alertamujer.resources.application.service.ResourceCallService;
import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCall>> getAllCalls() {
        return ResponseEntity.ok(resourceCallService.getAllCalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceCall> getCallById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return resourceCallService.getCallById(id, currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<ResourceCall>> getCallsByUser(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(resourceCallService.getCallsByUserProfile(userProfileId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResourceCall>> getMyCalls(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(resourceCallService.getCallsByUserProfile(currentUserId));
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
    public ResponseEntity<ResourceCall> createCall(@RequestBody ResourceCall call, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(resourceCallService.createCall(call, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceCall> updateCall(@PathVariable Long id, @RequestBody ResourceCall call, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(resourceCallService.updateCall(id, call, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCall(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        resourceCallService.deleteCall(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
