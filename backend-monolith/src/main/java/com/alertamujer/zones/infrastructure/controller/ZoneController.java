package com.alertamujer.zones.infrastructure.controller;

import com.alertamujer.zones.application.service.ZoneService;
import com.alertamujer.zones.domain.model.Zone;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZoneById(@PathVariable Long id) {
        return zoneService.getZoneById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Zone>> getZonesByCity(@PathVariable String city) {
        return ResponseEntity.ok(zoneService.getZonesByCity(city));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Zone>> getZonesByType(@PathVariable String type) {
        return ResponseEntity.ok(zoneService.getZonesByType(type));
    }

    @PostMapping
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.ok(zoneService.createZone(zone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Long id, @RequestBody Zone zone) {
        return ResponseEntity.ok(zoneService.updateZone(id, zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
