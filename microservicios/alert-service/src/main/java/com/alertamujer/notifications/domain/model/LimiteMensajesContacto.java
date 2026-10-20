package com.alertamujer.notifications.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de mensajes de un contacto en el chat de una alerta: como mucho uno cada 2 segundos y 30
 * cada 10 minutos por enlace, para que nadie pueda llenar el chat. Se cuenta en memoria.
 */
public class LimiteMensajesContacto {

    static final int MAXIMO_EN_VENTANA = 30;
    static final int MINUTOS_VENTANA = 10;
    static final int SEGUNDOS_ENTRE_MENSAJES = 2;

    private final Map<String, Deque<LocalDateTime>> enviosPorEnlace = new ConcurrentHashMap<>();

    /** Registra un envío del enlace o lanza IllegalArgumentException si supera el límite. */
    public void registrar(String claveEnlace, LocalDateTime ahora) {
        Deque<LocalDateTime> envios = enviosPorEnlace.computeIfAbsent(claveEnlace, k -> new ArrayDeque<>());
        synchronized (envios) {
            while (!envios.isEmpty() && envios.peekFirst().isBefore(ahora.minusMinutes(MINUTOS_VENTANA))) envios.pollFirst();
            if (!envios.isEmpty() && envios.peekLast().isAfter(ahora.minusSeconds(SEGUNDOS_ENTRE_MENSAJES))) {
                throw new IllegalArgumentException("Espera un momento antes de enviar otro mensaje");
            }
            if (envios.size() >= MAXIMO_EN_VENTANA) {
                throw new IllegalArgumentException("Enviaste muchos mensajes seguidos. Espera unos minutos");
            }
            envios.addLast(ahora);
        }
    }
}
