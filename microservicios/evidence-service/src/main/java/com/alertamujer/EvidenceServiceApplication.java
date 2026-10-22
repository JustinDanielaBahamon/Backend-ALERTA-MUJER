package com.alertamujer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Evidence Service: alert photos, videos and audio (Backblaze B2), SHA-256 fingerprint and retention. */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class EvidenceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvidenceServiceApplication.class, args);
    }
}
