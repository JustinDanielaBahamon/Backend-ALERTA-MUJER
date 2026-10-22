package com.alertamujer.devices.adapter.in.web;

import com.alertamujer.devices.adapter.in.web.dto.AdminDeviceView;
import com.alertamujer.devices.adapter.in.web.dto.DeviceRequest;
import com.alertamujer.devices.adapter.in.web.dto.DeviceResponse;
import com.alertamujer.devices.application.port.in.AdministrarDispositivosUseCase;
import com.alertamujer.devices.application.port.in.MisDispositivosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Adaptador de entrada REST de los dispositivos (la dueña y la administración). */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final MisDispositivosUseCase misDispositivos;
    private final AdministrarDispositivosUseCase administracion;

    public DeviceController(MisDispositivosUseCase misDispositivos, AdministrarDispositivosUseCase administracion) {
        this.misDispositivos = misDispositivos;
        this.administracion = administracion;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminDeviceView>> getAllDevices() {
        return ResponseEntity.ok(administracion.listAll().stream().map(AdminDeviceView::of).toList());
    }

    @GetMapping("/user")
    public ResponseEntity<List<DeviceResponse>> getMyDevices(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(misDispositivos.mios(currentUserId).stream().map(DeviceResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                           Authentication authentication) {
        // La administradora ve cualquier dispositivo, con los datos de su dueña
        if (esAdmin(authentication)) {
            return ResponseEntity.ok(AdminDeviceView.of(administracion.obtener(id)));
        }
        return ResponseEntity.ok(DeviceResponse.of(misDispositivos.uno(id, currentUserId)));
    }

    /** Cambia el estado de cualquier dispositivo: active, inactive, blocked o lost. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDeviceView> changeDeviceStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(AdminDeviceView.of(administracion.cambiarEstado(id, body.get("status"))));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(DeviceResponse.of(misDispositivos.registrar(request.aDatos(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @RequestBody DeviceRequest request,
                                                       @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(DeviceResponse.of(misDispositivos.reemplazar(id, request.aDatos(), currentUserId)));
    }

    /** La app informa los datos actuales del celular y su última conexión (solo la dueña). */
    @PutMapping("/{id}/info")
    public ResponseEntity<DeviceResponse> registrarUso(@PathVariable Long id, @RequestBody DeviceRequest request,
                                                       @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(DeviceResponse.of(misDispositivos.registrarUso(id, request.aDatos(), currentUserId)));
    }

    /**
     * Registra el token push (FCM) del dispositivo de la usuaria. Responde 204 para no devolver el
     * token; solo la dueña del dispositivo puede cambiarlo.
     */
    @PutMapping("/{id}/push-token")
    public ResponseEntity<Void> updatePushToken(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                @AuthenticationPrincipal Long currentUserId) {
        String token = body == null ? null : body.get("token");
        if (token == null || token.isBlank() || token.length() > 255) {
            throw new IllegalArgumentException("Token push no válido");
        }
        misDispositivos.cambiarTokenPush(id, token.trim(), currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId,
                                             Authentication authentication) {
        if (esAdmin(authentication)) {
            administracion.delete(id);
        } else {
            misDispositivos.delete(id, currentUserId);
        }
        return ResponseEntity.noContent().build();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
