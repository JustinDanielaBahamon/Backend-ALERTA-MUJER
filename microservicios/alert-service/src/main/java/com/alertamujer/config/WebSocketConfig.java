package com.alertamujer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] origenesPermitidos;

    @Override
    public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry config) {
        // Enable a simple message broker with prefixes /topic and /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Set the application destination prefix to /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WS endpoint with SockJS fallback and CORS
        registry.addEndpoint("/ws/alerts")
                .setAllowedOriginPatterns(java.util.Arrays.stream(origenesPermitidos).map(String::trim).toArray(String[]::new))
                .withSockJS();
    }
}