package com.alertamujer.shared.web;

import java.util.function.Supplier;

/**
 * Source of changes made outside an HTTP request (scheduled tasks), so the audit does not show
 * them as "no actor". Inside a request the source comes from RequestInfo.
 */
public final class AuditSource {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private AuditSource() {}

    public static String current() {
        return CURRENT.get();
    }

    /** Runs the task with this source (e.g. "Tarea automática · Expiración de alertas"). */
    public static <T> T runAs(String source, Supplier<T> task) {
        String previous = CURRENT.get();
        CURRENT.set(source);
        try {
            return task.get();
        } finally {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        }
    }
}
