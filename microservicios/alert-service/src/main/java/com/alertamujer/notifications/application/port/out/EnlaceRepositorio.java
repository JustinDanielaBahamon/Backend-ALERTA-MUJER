package com.alertamujer.notifications.application.port.out;

import com.alertamujer.notifications.domain.model.TrackingLink;

import java.util.Optional;

/** Puerto de salida: los enlaces de seguimiento. */
public interface EnlaceRepositorio {

    TrackingLink save(TrackingLink enlace);

    Optional<TrackingLink> porHash(String tokenHash);
}
