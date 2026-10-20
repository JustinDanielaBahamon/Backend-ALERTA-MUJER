package com.alertamujer.zones.adapter.in.web;

import com.alertamujer.zones.adapter.in.web.dto.ZoneRequest;
import com.alertamujer.zones.adapter.in.web.dto.ZoneResponse;
import com.alertamujer.zones.application.port.in.ZonasUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de las zonas del mapa. */
@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZonasUseCase zonas;

    public ZoneController(ZonasUseCase zonas) {
        this.zonas = zonas;
    }

    @GetMapping
    public ResponseEntity<List<ZoneResponse>> getAllZones() {
        return ResponseEntity.ok(zonas.todas().stream().map(ZoneResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponse> getZoneById(@PathVariable Long id) {
        return zonas.una(id).map(z -> ResponseEntity.ok(ZoneResponse.of(z))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<ZoneResponse>> getZonesByCity(@PathVariable String city) {
        return ResponseEntity.ok(zonas.deCiudad(city).stream().map(ZoneResponse::of).toList());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ZoneResponse>> getZonesByType(@PathVariable String type) {
        return ResponseEntity.ok(zonas.deTipo(type).stream().map(ZoneResponse::of).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZoneResponse> createZone(@RequestBody ZoneRequest request) {
        return ResponseEntity.ok(ZoneResponse.of(zonas.crear(request.aDatos())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZoneResponse> updateZone(@PathVariable Long id, @RequestBody ZoneRequest request) {
        return ResponseEntity.ok(ZoneResponse.of(zonas.reemplazar(id, request.aDatos())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zonas.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
