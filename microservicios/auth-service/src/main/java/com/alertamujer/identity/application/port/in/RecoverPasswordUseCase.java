package com.alertamujer.identity.application.port.in;

/**
 * Recuperación de contraseña con un código de 6 dígitos enviado al correo. La respuesta al pedir el
 * código es la misma exista o no la cuenta (no revela qué correos están registrados).
 */
public interface RecoverPasswordUseCase {

    void request(String email, String ip);

    /** Comprueba el código sin usarlo (paso intermedio de las pantallas). Cuenta como intento. */
    void verify(String email, String code);

    void reset(String email, String code, String newPassword);
}
