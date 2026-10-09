package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.service.LocationLogService;
import com.alertamujer.alerts.domain.model.LocationLog;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location-logs")
public class LocationLogController {

    private final LocationLogService locationLogService;

    public LocationLogController(LocationLogService locationLogService) {
        this.locationLogService = locationLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationLog>> getAll() {
        return ResponseEntity.ok(locationLogService.getAll());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<LocationLog>> getByUserProfileId(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(locationLogService.getByUserProfileId(userProfileId));
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<List<LocationLog>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(locationLogService.getByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<LocationLog> create(@RequestBody LocationLog locationLog, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(locationLogService.create(locationLog, currentUserId));
    }
}
