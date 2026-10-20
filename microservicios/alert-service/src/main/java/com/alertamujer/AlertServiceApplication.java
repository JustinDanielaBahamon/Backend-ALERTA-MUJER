package com.alertamujer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Alert Service: alertas SOS, ubicación, chat, notificaciones (correo y push), zonas y recursos de ayuda. */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertServiceApplication.class, args);
    }
}
