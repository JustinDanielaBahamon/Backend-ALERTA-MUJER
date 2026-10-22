package com.alertamujer.admin.domain.model;

/** Textos que exige la moderación: motivos obligatorios y explicaciones con un mínimo de caracteres. */
public final class TextoModeracion {

    public static final int MINIMO_EXPLICACION = 10;
    private static final int MAXIMO_EXPLICACION = 1000;

    private TextoModeracion() {
    }

    /** Explicación de al menos 10 caracteres (recortada a 1000); si no, IllegalArgumentException con el mensaje. */
    public static String explicacion(String value, String message) {
        String v = value == null ? "" : value.trim();
        if (v.length() < MINIMO_EXPLICACION) throw new IllegalArgumentException(message);
        return recortar(v, MAXIMO_EXPLICACION);
    }

    public static String requerido(String value, String message, int maximo) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return recortar(value.trim(), maximo);
    }

    public static String recortar(String v, int maximo) {
        return v.length() > maximo ? v.substring(0, maximo) : v;
    }
}
