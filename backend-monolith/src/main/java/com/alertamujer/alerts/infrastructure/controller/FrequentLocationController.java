package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.FrequentLocationService;
import com.alertamujer.alerts.domain.model.FrequentLocation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/frequent-locations")
public class FrequentLocationController {

    private final FrequentLocationService frequentLocationService;

    public FrequentLocationController(FrequentLocationService frequentLocationService) {
        this.frequentLocationService = frequentLocationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FrequentLocation>> getAll() {
        List<FrequentLocation> locations = frequentLocationService.getAllByUserProfileId(null);
        return ResponseEntity.ok(locations);
    }

    // userId = users.id (lo que envían web y móvil). Solo la dueña o un admin.
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<List<FrequentLocation>> getByUserId(@PathVariable Long userId) {
        List<FrequentLocation> locations = frequentLocationService.getActiveByUserId(userId);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/me")
    public ResponseEntity<List<FrequentLocation>> getMyLocations(@AuthenticationPrincipal Long currentUserId) {
        List<FrequentLocation> locations = frequentLocationService.getActiveByUserId(currentUserId);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FrequentLocation> getById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(frequentLocationService.getById(id, currentUserId));
    }

    @PostMapping
    public ResponseEntity<FrequentLocation> create(@RequestBody FrequentLocation frequentLocation, @AuthenticationPrincipal Long currentUserId) {
        FrequentLocation created = frequentLocationService.create(frequentLocation, currentUserId);
        return ResponseEntity.ok(created);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<FrequentLocation> update(@PathVariable Long id, @RequestBody FrequentLocation frequentLocation, @AuthenticationPrincipal Long currentUserId) {
        FrequentLocation updated = frequentLocationService.update(id, frequentLocation, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        frequentLocationService.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
