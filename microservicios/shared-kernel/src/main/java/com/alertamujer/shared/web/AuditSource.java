package com.alertamujer.shared.web;

import java.util.function.Supplier;

/**
 * Origen de los cambios hechos fuera de una petición HTTP (tareas programadas), para que la
 * auditoría no los muestre como "sin responsable". Dentro de una petición el origen sale de RequestInfo.
 */
public final class AuditSource {

    private static final ThreadLocal<String> ACTUAL = new ThreadLocal<>();

    private AuditSource() {}

    public static String actual() {
        return ACTUAL.get();
    }

    /** Ejecuta la tarea con este origen (p. ej. "Tarea automática · Expiración de alertas"). */
    public static <T> T con(String origen, Supplier<T> tarea) {
        String anterior = ACTUAL.get();
        ACTUAL.set(origen);
        try {
            return tarea.get();
        } finally {
            if (anterior == null) ACTUAL.remove(); else ACTUAL.set(anterior);
        }
    }
}
