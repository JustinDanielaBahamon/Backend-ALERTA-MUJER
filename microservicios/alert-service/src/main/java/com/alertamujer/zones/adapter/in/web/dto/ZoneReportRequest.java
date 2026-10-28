package com.alertamujer.zones.adapter.in.web.dto;

/** Body to report a zone. The author comes from the token: a userProfileId in the body is ignored. */
public record ZoneReportRequest(Long zoneId, String classification, String comment) {
}
