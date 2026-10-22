package com.alertamujer.notifications.application.port.in;

/** Envía por correo el código de recuperación de contraseña (lo genera Auth Service). */
public interface EnviarCodigoRecuperacionUseCase {

    void send(String email, String name, String code, int validityMinutes);
}
