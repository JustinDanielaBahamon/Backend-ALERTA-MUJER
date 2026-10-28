package com.alertamujer.admin.domain.model;

/** Texts moderation requires: mandatory reasons and explanations with a minimum length. */
public final class ModerationText {

    public static final int MIN_EXPLANATION = 10;
    private static final int MAX_EXPLANATION = 1000;

    private ModerationText() {
    }

    /** Explanation of at least 10 characters (truncated to 1000); otherwise IllegalArgumentException with the message. */
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
