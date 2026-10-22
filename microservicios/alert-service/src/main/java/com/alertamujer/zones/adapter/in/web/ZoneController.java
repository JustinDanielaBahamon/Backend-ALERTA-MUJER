package com.alertamujer.zones.adapter.in.web;

import com.alertamujer.zones.adapter.in.web.dto.ZoneRequest;
import com.alertamujer.zones.adapter.in.web.dto.ZoneResponse;
import com.alertamujer.zones.application.port.in.ZonesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de las zonas del mapa. */
@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZonesUseCase zones;

    public ZoneController(ZonesUseCase zones) {
        this.zones = zones;
    }

    @GetMapping
    public ResponseEntity<List<ZoneResponse>> getAllZones() {
        return ResponseEntity.ok(zones.all().stream().map(ZoneResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponse> getZoneById(@PathVariable Long id) {
        return zones.one(id).map(z -> ResponseEntity.ok(ZoneResponse.of(z))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<ZoneResponse>> getZonesByCity(@PathVariable String city) {
        return ResponseEntity.ok(zones.ofCity(city).stream().map(ZoneResponse::of).toList());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ZoneResponse>> getZonesByType(@PathVariable String type) {
        return ResponseEntity.ok(zones.ofType(type).stream().map(ZoneResponse::of).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZoneResponse> createZone(@RequestBody ZoneRequest request) {
        return ResponseEntity.ok(ZoneResponse.of(zones.create(request.toData())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZoneResponse> updateZone(@PathVariable Long id, @RequestBody ZoneRequest request) {
        return ResponseEntity.ok(ZoneResponse.of(zones.replace(id, request.toData())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zones.delete(id);
        return ResponseEntity.noContent().build();
    }
}
