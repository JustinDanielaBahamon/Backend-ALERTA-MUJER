package com.alertamujer.identity.application.port.out;

/** Output port: password hashing (BCrypt). */
public interface HashingPort {

    String hash(String password);

    boolean coincide(String password, String hash);
}
