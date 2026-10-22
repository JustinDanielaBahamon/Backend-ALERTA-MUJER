package com.alertamujer.notifications.application.port.out;

import com.alertamujer.notifications.domain.model.TrackingLink;

import java.util.Optional;

/** Output port: the tracking links. */
public interface LinkRepository {

    TrackingLink save(TrackingLink link);

    Optional<TrackingLink> byHash(String tokenHash);
}
