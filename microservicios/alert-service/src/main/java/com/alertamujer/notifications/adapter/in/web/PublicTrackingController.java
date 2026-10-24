package com.alertamujer.notifications.adapter.in.web;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase.TrackingView;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** REST input adapter: public tracking of an alert (opened by the contact from the email, without signing in). */
@RestController
public class PublicTrackingController {

    private final PublicTrackingUseCase tracking;

    public PublicTrackingController(PublicTrackingUseCase tracking) {
        this.tracking = tracking;
    }

    /**
     * v: anonymous identifier of the watching browser or app (to count the views). c: the delivery the
     * contact came from (email link or push), marked as seen in the history of the alert.
     */
    @GetMapping("/api/public/tracking/{code}")
    public ResponseEntity<TrackingView> query(@PathVariable String code,
                                                  @RequestParam(name = "v", required = false) String visitor,
                                                  @RequestParam(name = "c", required = false) Long recipientId) {
        LocalDateTime now = LocalDateTime.now();
        TrackingView view = tracking.query(code, visitor, now);
        tracking.reached(code, recipientId, true, now);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(view);
    }

    /** The contact's app confirms the alert push arrived (delivery c). */
    @PostMapping("/api/public/tracking/{code}/received")
    public ResponseEntity<Void> received(@PathVariable String code, @RequestParam("c") Long recipientId) {
        tracking.reached(code, recipientId, false, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }

    /** Invisible image of the alert email: when it loads, the contact opened the email (delivery c). */
    @GetMapping(value = "/api/public/tracking/{code}/opened.gif", produces = "image/gif")
    public ResponseEntity<byte[]> opened(@PathVariable String code, @RequestParam("c") Long recipientId) {
        tracking.reached(code, recipientId, false, LocalDateTime.now());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_GIF).body(PIXEL);
    }

    /** 1x1 transparent GIF. */
    private static final byte[] PIXEL = java.util.Base64.getDecoder()
            .decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    /** Alert chat for the contact. after: id of the last message she already has (0 = all). */
    @GetMapping("/api/public/tracking/{code}/messages")
    public ResponseEntity<List<AlertMessageView>> messages(@PathVariable String code,
                                                           @RequestParam(defaultValue = "0") Long after) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(tracking.messages(code, after, LocalDateTime.now()));
    }

    @PostMapping("/api/public/tracking/{code}/messages")
    public ResponseEntity<AlertMessageView> sendMessage(@PathVariable String code, @RequestBody ContactMessage message) {
        return ResponseEntity.ok(tracking.sendMessage(code, message.name(), message.body(), LocalDateTime.now()));
    }

    /** name: how the contact introduces herself (the link does not say who she is). */
    public record ContactMessage(String name, String body) {}

    /**
     * Alert video or audio for the contact. With S3 storage it redirects to a signed link
     * that expires in minutes; on disk it serves the file (with Range, to seek).
     */
    @GetMapping("/api/public/tracking/{code}/evidence/{id}")
    public ResponseEntity<Resource> evidence(@PathVariable String code, @PathVariable Long id) {
        EvidenceApi.EvidenceFile file = tracking.evidence(code, id, LocalDateTime.now());
        if (file.temporaryLink().isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(file.temporaryLink().get())
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .cacheControl(CacheControl.noStore())
                .body(file.resource());
    }
}
