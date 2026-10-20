package com.alertamujer.shared.exception;

import java.util.Map;

/**
 * Datos que ya usa otra cuenta (correo, teléfono, documento...). Responde 409 con un mensaje por
 * campo para que el formulario lo muestre debajo de cada uno.
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
