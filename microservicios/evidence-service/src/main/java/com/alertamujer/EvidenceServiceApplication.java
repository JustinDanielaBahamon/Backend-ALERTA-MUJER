package com.alertamujer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Evidence Service: fotos, videos y audios de las alertas (Backblaze B2), huella SHA-256 y retención. */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class EvidenceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvidenceServiceApplication.class, args);
    }
}
