package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.SafeTripController.SafeTripView;
import com.alertamujer.alerts.application.port.in.AutoProtectionUseCase;
import com.alertamujer.alerts.application.port.in.AutoProtectionUseCase.AutoHeartbeat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST input adapter: automatic protection. She turns it on and off with her session; her phone
 * then starts, keeps and ends it by itself with its own key (header X-Protection-Key), also when her
 * session expired because she did not open the app for days.
 */
@RestController
public class AutoProtectionController {

    static final String KEY_HEADER = "X-Protection-Key";

    private final AutoProtectionUseCase protection;

    public AutoProtectionController(AutoProtectionUseCase protection) {
        this.protection = protection;
    }

    // ── Her session ──

    /** Turns it on in this phone. The key is returned only now: the phone keeps it. */
    @PostMapping("/api/auto-protection/enable")
    public ResponseEntity<Map<String, String>> enable(@RequestBody DeviceRequest r, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(Map.of("key", protection.enable(userId, r.deviceId())));
    }

    @PostMapping("/api/auto-protection/disable")
    public ResponseEntity<Void> disable(@RequestBody DeviceRequest r, @AuthenticationPrincipal Long userId) {
        protection.disable(userId, r.deviceId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/auto-protection")
    public ResponseEntity<Map<String, Boolean>> status(@RequestParam Long deviceId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(Map.of("enabled", protection.enabled(userId, deviceId)));
    }

    // ── Her phone, with its key ──

    @PostMapping("/api/public/auto-protection/start")
    public ResponseEntity<SafeTripView> start(@RequestHeader(KEY_HEADER) String key, @RequestBody StartRequest r) {
        return ResponseEntity.ok(SafeTripView.of(protection.startAuto(key, r.latitude(), r.longitude())));
    }

    @PostMapping("/api/public/auto-protection/{id}/heartbeat")
    public ResponseEntity<SafeTripView> heartbeat(@RequestHeader(KEY_HEADER) String key, @PathVariable Long id,
                                                  @RequestBody HeartbeatRequest r) {
        return ResponseEntity.ok(SafeTripView.of(protection.autoHeartbeat(key, id,
                new AutoHeartbeat(r.latitude(), r.longitude(), r.accuracy(), r.recordedAt(), Boolean.TRUE.equals(r.lowBattery())))));
    }

    @PostMapping("/api/public/auto-protection/{id}/end")
    public ResponseEntity<SafeTripView> end(@RequestHeader(KEY_HEADER) String key, @PathVariable Long id) {
        return ResponseEntity.ok(SafeTripView.of(protection.endAuto(key, id)));
    }

    @PostMapping("/api/public/auto-protection/{id}/no-answer")
    public ResponseEntity<SafeTripView> noAnswer(@RequestHeader(KEY_HEADER) String key, @PathVariable Long id) {
        return ResponseEntity.ok(SafeTripView.of(protection.noAnswer(key, id)));
    }

    @PostMapping("/api/public/auto-protection/{id}/unconfirmed")
    public ResponseEntity<SafeTripView> unconfirmed(@RequestHeader(KEY_HEADER) String key, @PathVariable Long id) {
        return ResponseEntity.ok(SafeTripView.of(protection.unconfirmed(key, id)));
    }

    @PostMapping("/api/public/auto-protection/{id}/duress")
    public ResponseEntity<SafeTripView> duress(@RequestHeader(KEY_HEADER) String key, @PathVariable Long id) {
        return ResponseEntity.ok(SafeTripView.of(protection.duress(key, id)));
    }

    public record DeviceRequest(Long deviceId) {}

    public record StartRequest(BigDecimal latitude, BigDecimal longitude) {}

    public record HeartbeatRequest(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy,
                                   LocalDateTime recordedAt, Boolean lowBattery) {}
}
