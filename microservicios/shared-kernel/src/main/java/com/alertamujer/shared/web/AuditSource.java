package com.alertamujer.shared.web;

import java.util.function.Supplier;

/**
 * Origen de los cambios hechos fuera de una petición HTTP (tareas programadas), para que la
 * auditoría no los muestre como "sin responsable". Dentro de una petición el origen sale de RequestInfo.
 */
public final class AuditSource {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private AuditSource() {}

    public static String current() {
        return CURRENT.get();
    }

    /** Ejecuta la tarea con este origen (p. ej. "Tarea automática · Expiración de alertas"). */
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
