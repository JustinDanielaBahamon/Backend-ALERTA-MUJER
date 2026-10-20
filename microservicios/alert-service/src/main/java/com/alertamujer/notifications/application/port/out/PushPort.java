package com.alertamujer.notifications.application.port.out;

import java.util.List;
import java.util.Map;

/** Puerto de salida: push de alta prioridad con Firebase. */
public interface PushPort {

    /** Devuelve los tokens a los que no se pudo enviar. */
    List<String> enviar(List<String> tokens, Map<String, String> datos);
}
