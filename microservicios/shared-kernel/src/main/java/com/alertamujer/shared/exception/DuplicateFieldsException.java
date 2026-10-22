package com.alertamujer.shared.exception;

import java.util.Map;

/**
 * Data already used by another account (email, phone, document...). Returns 409 with one message per
 * field so the form shows it under each one.
 */
public class DuplicateFieldsException extends ConflictException {
    private final Map<String, String> fieldErrors;

    public DuplicateFieldsException(Map<String, String> fieldErrors) {
        super("Datos ya registrados");
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
