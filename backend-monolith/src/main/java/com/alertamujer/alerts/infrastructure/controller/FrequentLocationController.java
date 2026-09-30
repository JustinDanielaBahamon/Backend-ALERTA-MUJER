package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.FrequentLocationService;
import com.alertamujer.alerts.domain.model.FrequentLocation;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<FrequentLocation>> getAll() {
        List<FrequentLocation> locations = frequentLocationService.getAllByUserProfileId(null);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FrequentLocation>> getByUserId(@PathVariable Long userId) {
        List<FrequentLocation> locations = frequentLocationService.getActiveByUserProfileId(userId);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FrequentLocation> getById(@PathVariable Long id) {
        FrequentLocation location = frequentLocationService.getById(id);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }

    @PostMapping
    public ResponseEntity<FrequentLocation> create(@RequestBody FrequentLocation frequentLocation) {
        FrequentLocation created = frequentLocationService.create(frequentLocation);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FrequentLocation> update(@PathVariable Long id, @RequestBody FrequentLocation frequentLocation) {
        FrequentLocation updated = frequentLocationService.update(id, frequentLocation);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        frequentLocationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
