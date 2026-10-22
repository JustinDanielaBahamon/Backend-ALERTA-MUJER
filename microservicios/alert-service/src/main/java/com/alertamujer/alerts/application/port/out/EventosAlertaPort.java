package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.Alert;

import java.time.LocalDateTime;

/**
 * Puerto de salida: avisa al resto del sistema lo que pasa con una alerta. El módulo de
 * notificaciones envía los correos y push después de confirmar la transacción.
 */
public interface EventosAlertaPort {

    /** Se activó la alerta y ya quedaron registrados sus destinatarios. */
    void alertaActivada(Alert alert);

    /** Terminó una alerta activa (atendida, cancelada o expirada). */
    void alertaTerminada(Alert alert, LocalDateTime when);
}
