package com.alertamujer.identity.infrastructure.controller.dto;

import com.alertamujer.identity.infrastructure.validation.PasswordComplexity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    // @PasswordComplexity(message = "La contraseña debe contener al menos 8 caracteres, una mayúscula, un número y un carácter especial")
    private String password;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    private String tipoDocumento;

    private String numeroDocumento;

    private String fechaNacimiento;
}