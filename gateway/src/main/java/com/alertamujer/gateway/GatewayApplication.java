package com.alertamujer.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

@RestController
class GatewayController {

    private final WebClient webClient = WebClient.create();

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("{\"status\":\"ok\",\"service\":\"gateway\"}");
    }
}
