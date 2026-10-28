package com.alertamujer.notifications.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message limit for a contact in an alert chat: at most one every 2 seconds and 30
 * every 10 minutes per link, so nobody can flood the chat. Counted in memory.
 */
public class ContactMessageLimit {

    static final int MAX_IN_WINDOW = 30;
    static final int WINDOW_MINUTES = 10;
    static final int SECONDS_BETWEEN_MESSAGES = 2;

    private final Map<String, Deque<LocalDateTime>> deliveriesByLink = new ConcurrentHashMap<>();

    /** Records a send through the link or throws IllegalArgumentException if over the limit. */
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
