package com.alertamujer.devices.adapter.in.web;

import com.alertamujer.devices.adapter.in.web.dto.AdminDeviceView;
import com.alertamujer.devices.adapter.in.web.dto.DeviceRequest;
import com.alertamujer.devices.adapter.in.web.dto.DeviceResponse;
import com.alertamujer.devices.application.port.in.ManageDevicesUseCase;
import com.alertamujer.devices.application.port.in.MyDevicesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST input adapter for devices (the owner and the administration). */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final MyDevicesUseCase myDevices;
    private final ManageDevicesUseCase administration;

    public DeviceController(MyDevicesUseCase myDevices, ManageDevicesUseCase administration) {
        this.myDevices = myDevices;
        this.administration = administration;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminDeviceView>> getAllDevices() {
        return ResponseEntity.ok(administration.listAll().stream().map(AdminDeviceView::of).toList());
    }

    @GetMapping("/user")
    public ResponseEntity<List<DeviceResponse>> getMyDevices(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(myDevices.mine(currentUserId).stream().map(DeviceResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                           Authentication authentication) {
        // The admin sees any device, with its owner's data
        if (esAdmin(authentication)) {
            return ResponseEntity.ok(AdminDeviceView.of(administration.get(id)));
        }
        return ResponseEntity.ok(DeviceResponse.of(myDevices.one(id, currentUserId)));
    }

    /** Changes the status of any device: active, inactive, blocked or lost. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDeviceView> changeDeviceStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(AdminDeviceView.of(administration.changeStatus(id, body.get("status"))));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(DeviceResponse.of(myDevices.registrar(request.toData(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @RequestBody DeviceRequest request,
                                                       @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(DeviceResponse.of(myDevices.replace(id, request.toData(), currentUserId)));
    }

    /** The app reports the phone's current data and its last connection (owner only). */
    @PutMapping("/{id}/info")
    public ResponseEntity<DeviceResponse> recordUse(@PathVariable Long id, @RequestBody DeviceRequest request,
                                                       @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(DeviceResponse.of(myDevices.recordUse(id, request.toData(), currentUserId)));
    }

    /**
     * Registers the push token (FCM) of the user's device. Returns 204 so the token is not sent back;
     * only the device owner can change it.
     */
    @PutMapping("/{id}/push-token")
    public ResponseEntity<Void> updatePushToken(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                @AuthenticationPrincipal Long currentUserId) {
        String token = body == null ? null : body.get("token");
        if (token == null || token.isBlank() || token.length() > 255) {
            throw new IllegalArgumentException("Token push no válido");
        }
        myDevices.changePushToken(id, token.trim(), currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                             Authentication authentication) {
        if (esAdmin(authentication)) {
            administration.delete(id);
        } else {
            myDevices.delete(id, currentUserId);
        }
        return ResponseEntity.noContent().build();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
