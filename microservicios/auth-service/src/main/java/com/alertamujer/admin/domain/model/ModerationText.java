package com.alertamujer.admin.domain.model;

/** Textos que exige la moderación: motivos obligatorios y explicaciones con un mínimo de caracteres. */
public final class ModerationText {

    public static final int MIN_EXPLANATION = 10;
    private static final int MAX_EXPLANATION = 1000;

    private ModerationText() {
    }

    /** Explicación de al menos 10 caracteres (recortada a 1000); si no, IllegalArgumentException con el mensaje. */
    public static String explanation(String value, String message) {
        String v = value == null ? "" : value.trim();
        if (v.length() < MIN_EXPLANATION) throw new IllegalArgumentException(message);
        return truncate(v, MAX_EXPLANATION);
    }

    public static String required(String value, String message, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return truncate(value.trim(), max);
    }

    public static String truncate(String v, int max) {
        return v.length() > max ? v.substring(0, max) : v;
    }
}
