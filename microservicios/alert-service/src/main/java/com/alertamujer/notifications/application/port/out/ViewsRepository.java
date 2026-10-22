package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/** Puerto de salida: quién abrió el seguimiento de cada alerta. */
public interface ViewsRepository {

    /** Registra (o renueva) la visita de ese visitante a la alerta. */
    void registrar(Long alertId, String visitor, LocalDateTime now);

    /** total: visitantes distintos; viendoAhora: los que la abrieron desde "recientes". */
    ViewCount count(Long alertId, LocalDateTime recent);

    record ViewCount(int total, @com.fasterxml.jackson.annotation.JsonProperty("viendoAhora") int watchingNow) {}
}
