package com.alertamujer.devices.infrastructure.controller;

import com.alertamujer.devices.application.service.DeviceService;
import com.alertamujer.devices.domain.model.Device;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(deviceService.getDeviceById(id, currentUserId));
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(deviceService.createDevice(device, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(deviceService.updateDevice(id, device, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        deviceService.deleteDevice(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
