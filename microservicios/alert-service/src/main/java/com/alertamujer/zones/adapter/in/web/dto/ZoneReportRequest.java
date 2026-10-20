package com.alertamujer.zones.adapter.in.web.dto;

/** Body para reportar una zona. La autora sale del token: un userProfileId en el body se ignora. */
public record ZoneReportRequest(Long zoneId, String classification, String comment) {
}
