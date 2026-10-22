package com.alertamujer.resources.adapter.in.web;

import com.alertamujer.resources.adapter.in.web.dto.ResourceCallRequest;
import com.alertamujer.resources.adapter.in.web.dto.ResourceCallResponse;
import com.alertamujer.resources.application.port.in.CallsUseCase;
import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST input adapter for calls to resources. */
@RestController
@RequestMapping("/api/resource-calls")
public class ResourceCallController {

    private final CallsUseCase calls;

    public ResourceCallController(CallsUseCase calls) {
        this.calls = calls;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCallResponse>> getAllCalls() {
        return ResponseEntity.ok(views(calls.all()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceCallResponse> getCallById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return calls.one(id, currentUserId).map(c -> ResponseEntity.ok(ResourceCallResponse.of(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // userId = users.id. Owner or administration only.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<List<ResourceCallResponse>> getCallsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(views(calls.mias(userId)));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResourceCallResponse>> getMyCalls(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(views(calls.mias(currentUserId)));
    }

    @GetMapping("/resource/{emergencyResourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCallResponse>> getCallsByResource(@PathVariable Long emergencyResourceId) {
        return ResponseEntity.ok(views(calls.ofResource(emergencyResourceId)));
    }

    @GetMapping("/alert/{alertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCallResponse>> getCallsByAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(views(calls.ofAlert(alertId)));
    }

    @PostMapping
    public ResponseEntity<ResourceCallResponse> createCall(@RequestBody ResourceCallRequest r, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ResourceCallResponse.of(calls.registrar(r.emergencyResourceId(), r.alertId(), r.deviceId(),
                r.telephoneDialed(), r.status(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceCallResponse> updateCall(@PathVariable Long id, @RequestBody ResourceCallRequest r,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ResourceCallResponse.of(calls.update(id, r.status(), r.endedAt(), r.durationSeconds(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCall(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        calls.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    private static List<ResourceCallResponse> views(List<ResourceCall> list) {
        return list.stream().map(ResourceCallResponse::of).toList();
    }
}
