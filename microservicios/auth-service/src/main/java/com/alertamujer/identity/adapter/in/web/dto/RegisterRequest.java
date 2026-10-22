package com.alertamujer.identity.adapter.in.web.dto;

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
    // The app and the web send it as "nombre"
    @com.fasterxml.jackson.annotation.JsonProperty("nombre")
    private String name;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @com.fasterxml.jackson.annotation.JsonProperty("telefono") private String telephone;

    @Size(max = 30, message = "El tipo de documento no puede exceder 30 caracteres")
    @com.fasterxml.jackson.annotation.JsonProperty("tipoDocumento") private String documentType;

    @Size(max = 30, message = "El número de documento no puede exceder 30 caracteres")
    @com.fasterxml.jackson.annotation.JsonProperty("numeroDocumento") private String documentNumber;

    /** DD/MM/AAAA, como lo escriben los formularios. */
    @com.fasterxml.jackson.annotation.JsonProperty("fechaNacimiento") private String birthDate;

    /** Casilla "Acepto los términos y la política de privacidad". */
    @com.fasterxml.jackson.annotation.JsonProperty("aceptaTerminos") private Boolean acceptsTerms;
}