package com.alertamujer.alerts.application.port.out;

import java.util.List;
import java.util.Map;

/** Puerto de salida: notificaciones push (Firebase). */
public interface PushPort {

    /** Envía un push de datos (silencioso). Devuelve los tokens a los que no se pudo enviar. */
    List<String> sendData(List<String> tokens, Map<String, String> data);
}
