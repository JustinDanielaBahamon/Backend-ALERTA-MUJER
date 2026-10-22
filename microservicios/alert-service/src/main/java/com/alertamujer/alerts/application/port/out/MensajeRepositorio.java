package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.AlertMessage;

import java.util.List;

/** Puerto de salida: el chat de cada alerta. */
public interface MensajeRepositorio {

    /** Mensajes posteriores a afterId (0 = todos), del más antiguo al más nuevo. */
    List<AlertMessage> posteriores(Long alertId, long afterId);

    long count(Long alertId);

    AlertMessage save(AlertMessage message);
}
