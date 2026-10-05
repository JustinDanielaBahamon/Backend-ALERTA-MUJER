package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location-logs")
public class LocationLogController {

    private final LocationLogRepository locationLogRepository;

    public LocationLogController(LocationLogRepository locationLogRepository) {
        this.locationLogRepository = locationLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<LocationLog>> getAll() {
        return ResponseEntity.ok(locationLogRepository.findAll());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<LocationLog>> getByUser(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(locationLogRepository.findByUserProfileId(userProfileId));
    }
}
