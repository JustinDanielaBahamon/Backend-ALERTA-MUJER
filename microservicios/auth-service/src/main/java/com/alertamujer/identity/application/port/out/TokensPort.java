package com.alertamujer.identity.application.port.out;

/** Puerto de salida: emisión y lectura de los JWT de sesión. */
public interface TokensPort {

    /** Token de acceso (24 horas) con el rol de la persona. */
    String access(Long userId, String role);

    /** Token de renovación (15 días) para renovar la sesión sin pedir la contraseña. */
    String refresh(Long userId);

    /**
     * users.id de un token de renovación válido.
     * @throws RuntimeException si la firma no es válida, venció o no es de renovación
     */
    Long userForRefresh(String token);
}
