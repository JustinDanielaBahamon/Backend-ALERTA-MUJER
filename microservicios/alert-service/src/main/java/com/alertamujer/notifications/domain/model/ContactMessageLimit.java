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
public class ContactMessageLimit {

    static final int MAX_IN_WINDOW = 30;
    static final int WINDOW_MINUTES = 10;
    static final int SECONDS_BETWEEN_MESSAGES = 2;

    private final Map<String, Deque<LocalDateTime>> deliveriesByLink = new ConcurrentHashMap<>();

    /** Registra un envío del enlace o lanza IllegalArgumentException si supera el límite. */
    public void registrar(String linkKey, LocalDateTime now) {
        Deque<LocalDateTime> deliveries = deliveriesByLink.computeIfAbsent(linkKey, k -> new ArrayDeque<>());
        synchronized (deliveries) {
            while (!deliveries.isEmpty() && deliveries.peekFirst().isBefore(now.minusMinutes(WINDOW_MINUTES))) deliveries.pollFirst();
            if (!deliveries.isEmpty() && deliveries.peekLast().isAfter(now.minusSeconds(SECONDS_BETWEEN_MESSAGES))) {
                throw new IllegalArgumentException("Espera un momento antes de enviar otro mensaje");
            }
            if (deliveries.size() >= MAX_IN_WINDOW) {
                throw new IllegalArgumentException("Enviaste muchos mensajes seguidos. Espera unos minutos");
            }
            deliveries.addLast(now);
        }
    }
}
