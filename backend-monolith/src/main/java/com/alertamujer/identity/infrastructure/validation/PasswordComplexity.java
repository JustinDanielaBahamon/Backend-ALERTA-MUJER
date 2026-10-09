package com.alertamujer.identity.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordComplexityValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordComplexity {
    String message() default "La contraseña debe contener al menos 8 caracteres, una mayúscula, un número y un carácter especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
