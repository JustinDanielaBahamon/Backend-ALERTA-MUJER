package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.application.port.in.TrayectosUseCase;
import com.alertamujer.alerts.application.port.in.TrayectosUseCase.NuevoTrayecto;
import com.alertamujer.alerts.domain.model.SafeTrip;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Adaptador de entrada REST: "Avísame si no llego". Todo es de la usuaria en sesión. */
@RestController
@RequestMapping("/api/safe-trips")
public class SafeTripController {

    private final TrayectosUseCase trayectos;

    public SafeTripController(TrayectosUseCase trayectos) {
        this.trayectos = trayectos;
    }

    @PostMapping
    public ResponseEntity<SafeTripView> start(@RequestBody StartRequest r, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trayectos.iniciar(
                new NuevoTrayecto(r.destination(), r.minutes(), r.deviceId(), r.latitude(), r.longitude()), userId)));
    }

    /** El trayecto en curso, o 204 si no hay. */
    @GetMapping("/active")
    public ResponseEntity<SafeTripView> active(@AuthenticationPrincipal Long userId) {
        return trayectos.activo(userId).map(t -> ResponseEntity.ok(SafeTripView.of(t)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/arrived")
    public ResponseEntity<SafeTripView> arrived(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trayectos.llegue(id, userId)));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<SafeTripView> extend(@PathVariable Long id, @RequestBody ExtendRequest r,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trayectos.extender(id, r.minutes(), userId)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SafeTripView> cancel(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trayectos.cancelar(id, userId)));
    }

    public record StartRequest(String destination, int minutes, Long deviceId, BigDecimal latitude, BigDecimal longitude) {}

    public record ExtendRequest(int minutes) {}

    public record SafeTripView(Long id, String destination, BigDecimal destinationLatitude, BigDecimal destinationLongitude,
                               LocalDateTime startedAt, LocalDateTime expectedArrival, String status, Long alertId) {
        static SafeTripView of(SafeTrip t) {
            return new SafeTripView(t.getId(), t.getDestination(), t.getDestinationLatitude(), t.getDestinationLongitude(),
                    t.getStartedAt(), t.getExpectedArrival(), t.getStatus(), t.getAlertId());
        }
    }
}
