package com.alertamujer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String[] passwords = {
            "Mujer123!",
            "Admin2026#",
            "Camila*1",
            "Raquel2026$",
            "Alerta123@"
        };

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println(password + " -> " + hash);
        }
    }
}
