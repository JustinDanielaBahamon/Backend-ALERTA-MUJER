package com.alertamujer.devices.infrastructure.controller;

import com.alertamujer.devices.application.service.AdminDeviceService;
import com.alertamujer.devices.application.service.DeviceService;
import com.alertamujer.devices.domain.model.Device;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final AdminDeviceService adminDeviceService;

    public DeviceController(DeviceService deviceService, AdminDeviceService adminDeviceService) {
        this.deviceService = deviceService;
        this.adminDeviceService = adminDeviceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminDeviceService.AdminDeviceView>> getAllDevices() {
        return ResponseEntity.ok(adminDeviceService.listar());
    }

    @GetMapping("/user")
    public ResponseEntity<List<Device>> getMyDevices(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(deviceService.getDevicesByCurrentUser(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                           Authentication authentication) {
        // La administradora ve cualquier dispositivo, con los datos de su dueña
        if (esAdmin(authentication)) {
            return ResponseEntity.ok(adminDeviceService.obtener(id));
        }
        return ResponseEntity.ok(deviceService.getDeviceById(id, currentUserId));
    }

    /** Cambia el estado de cualquier dispositivo: active, inactive, blocked o lost. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDeviceService.AdminDeviceView> changeDeviceStatus(@PathVariable Long id,
                                                                              @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(adminDeviceService.cambiarEstado(id, body.get("status")));
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(deviceService.createDevice(device, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(deviceService.updateDevice(id, device, currentUserId));
    }

    /**
     * Registra el token push (FCM) del dispositivo de la usuaria. Responde 204 para no devolver el
     * token; solo la dueña del dispositivo puede cambiarlo.
     */
    @PutMapping("/{id}/push-token")
    public ResponseEntity<Void> updatePushToken(@PathVariable Long id, @RequestBody java.util.Map<String, String> body,
                                                @AuthenticationPrincipal Long currentUserId) {
        String token = body == null ? null : body.get("token");
        if (token == null || token.isBlank() || token.length() > 255) {
            throw new IllegalArgumentException("Token push no válido");
        }
        deviceService.updateFcmToken(id, token.trim(), currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                             Authentication authentication) {
        if (esAdmin(authentication)) {
            adminDeviceService.eliminar(id);
        } else {
            deviceService.deleteDevice(id, currentUserId);
        }
        return ResponseEntity.noContent().build();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
