package com.alertamujer.alerts.application.port.out;

import java.util.List;
import java.util.Map;

/** Puerto de salida: notificaciones push (Firebase). */
public interface PushPort {

    /** Sends a data (silent) push. Returns the tokens that could not be reached. */
    List<String> sendData(List<String> tokens, Map<String, String> data);
}
