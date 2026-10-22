package com.alertamujer.notifications.application.port.in;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public tracking of an alert with the email link (without signing in). If the link does not
 * exist, was revoked or expired, throws ResourceNotFoundException.
 */
public interface PublicTrackingUseCase {

    /** Creates a link for the alert and returns the code (it cannot be recovered later). */
    String createLink(Long alertId);

    TrackingView query(String code, LocalDateTime now);

    /**
     * Like query, and also records that this visitor (anonymous identifier of the
     * browser or the app) opened the tracking: the user sees how many are watching.
     */
    TrackingView query(String code, String visitor, LocalDateTime now);

    /** File of a recording of the link's alert. */
    EvidenceFile evidence(String code, Long evidenceId, LocalDateTime now);

    List<AlertMessageView> messages(String code, Long afterId, LocalDateTime now);

    /** Message from the contact; with a per-link limit so nobody floods the chat. */
    AlertMessageView sendMessage(String code, String name, String text, LocalDateTime now);

    /** Only the user's first name: the link may be forwarded. */
    record TrackingView(String firstName, String status, boolean active, LocalDateTime startedAt,
                        LocalDateTime endedAt, List<LocationPoint> route, String emergencyNumber,
                        List<PublicEvidence> evidence) {
        public LocationPoint lastPoint() {
            return route.isEmpty() ? null : route.get(route.size() - 1);
        }
    }

    /** Evidence the contact sees: opened with /api/public/tracking/{code}/evidence/{id}. */
    record PublicEvidence(Long id, String mediaType, String contentType, LocalDateTime createdAt) {}
}
