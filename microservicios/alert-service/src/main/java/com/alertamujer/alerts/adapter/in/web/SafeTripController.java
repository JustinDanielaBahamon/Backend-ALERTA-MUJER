package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.application.port.in.TripsUseCase;
import com.alertamujer.alerts.application.port.in.TripsUseCase.NewTrip;
import com.alertamujer.alerts.domain.model.SafeTrip;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** REST input adapter: "Avísame si no llego". Everything belongs to the signed-in user. */
@RestController
@RequestMapping("/api/safe-trips")
public class SafeTripController {

    private final TripsUseCase trips;

    public SafeTripController(TripsUseCase trips) {
        this.trips = trips;
    }

    @PostMapping
    public ResponseEntity<SafeTripView> start(@RequestBody StartRequest r, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trips.start(
                new NewTrip(r.destination(), r.minutes(), r.deviceId(), r.latitude(), r.longitude()), userId)));
    }

    /** The ongoing trip, or 204 if none. */
    @GetMapping("/active")
    public ResponseEntity<SafeTripView> active(@AuthenticationPrincipal Long userId) {
        return trips.active(userId).map(t -> ResponseEntity.ok(SafeTripView.of(t)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/arrived")
    public ResponseEntity<SafeTripView> arrived(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trips.arrived(id, userId)));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<SafeTripView> extend(@PathVariable Long id, @RequestBody ExtendRequest r,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trips.extender(id, r.minutes(), userId)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SafeTripView> cancel(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trips.cancel(id, userId)));
    }

    /** Signal of life from her phone during the trip (every few minutes, also queued while offline). */
    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<SafeTripView> heartbeat(@PathVariable Long id, @RequestBody HeartbeatRequest r,
                                                  @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SafeTripView.of(trips.heartbeat(id,
                new TripsUseCase.Heartbeat(r.latitude(), r.longitude(), r.accuracy(), r.recordedAt()), userId)));
    }

    public record HeartbeatRequest(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}

    public record StartRequest(String destination, int minutes, Long deviceId, BigDecimal latitude, BigDecimal longitude) {}

    public record ExtendRequest(int minutes) {}

    public record SafeTripView(Long id, String destination, BigDecimal destinationLatitude, BigDecimal destinationLongitude,
                               LocalDateTime startedAt, LocalDateTime expectedArrival, String status, Long alertId,
                               LocalDateTime lastSeenAt, String alertReason) {
        static SafeTripView of(SafeTrip t) {
            return new SafeTripView(t.getId(), t.getDestination(), t.getDestinationLatitude(), t.getDestinationLongitude(),
                    t.getStartedAt(), t.getExpectedArrival(), t.getStatus(), t.getAlertId(), t.getLastSeenAt(),
                    t.getAlertReason());
        }
    }
}
