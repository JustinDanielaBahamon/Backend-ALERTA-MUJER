package com.alertamujer.identity.application.port.out;

/** Puerto de salida: hash de contraseñas (BCrypt). */
public interface CifradoPort {

    String cifrar(String contrasena);

    boolean coincide(String contrasena, String hash);
}
