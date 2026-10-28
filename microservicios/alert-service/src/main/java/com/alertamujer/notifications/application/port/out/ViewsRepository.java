package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/** Output port: who opened the tracking of each alert. */
public interface ViewsRepository {

    /** Records (or renews) that visitor's visit to the alert. */
    void registrar(Long alertId, String visitor, LocalDateTime now);

    /** total: distinct visitors; watchingNow: those who opened it since "recent". */
    ViewCount count(Long alertId, LocalDateTime recent);

    record ViewCount(int total, @com.fasterxml.jackson.annotation.JsonProperty("viendoAhora") int watchingNow) {}
}
