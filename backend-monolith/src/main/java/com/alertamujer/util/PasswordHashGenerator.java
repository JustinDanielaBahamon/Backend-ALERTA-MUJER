package com.alertamujer.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "Admin2024!";
        String hash = encoder.encode(password);
        
        System.out.println("=================================");
        System.out.println("Password: " + password);
        System.out.println("Hash generado: " + hash);
        System.out.println("=================================");
        System.out.println("");
        System.out.println("Verificación:");
        System.out.println("¿Coincide? " + encoder.matches(password, hash));
        System.out.println("=================================");
    }
}
