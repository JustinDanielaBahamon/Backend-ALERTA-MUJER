package com.alertamujer.identity.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordComplexityValidator implements ConstraintValidator<PasswordComplexity, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Verificar longitud mínima de 8 caracteres
        if (password.length() < 8) {
            return false;
        }

        // Verificar al menos una mayúscula
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Verificar al menos un número
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Verificar al menos un carácter especial
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return false;
        }

        return true;
    }
}
