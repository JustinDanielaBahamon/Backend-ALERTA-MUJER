package com.alertamujer.identity.application.port.out;

/** Puerto de salida: hash de contraseñas (BCrypt). */
public interface HashingPort {

    String hash(String password);

    boolean coincide(String password, String hash);
}
