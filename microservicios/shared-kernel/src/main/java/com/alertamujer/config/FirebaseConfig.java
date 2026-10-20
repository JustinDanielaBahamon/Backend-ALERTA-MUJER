package com.alertamujer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:classpath:firebase-service-account.json}")
    private String firebaseConfigPath;

    public FirebaseConfig(Environment env) {
        this.firebaseConfigPath = env.getProperty("firebase.config.path", "classpath:firebase-service-account.json");
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String path = firebaseConfigPath;
        FileInputStream serviceAccount = null;
        FirebaseOptions options = null;

        // If the path does not start with "classpath:", treat it as a file path
        // This allows loading from Docker-mounted volumes
        if (!path.startsWith("classpath:")) {
            // File path - could be mounted volume or absolute path
            serviceAccount = new FileInputStream(path);
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        } else {
            // Classpath resource - use ClassPathResource for reliability
            try {
                ClassPathResource resource = new ClassPathResource(path.replace("classpath", ""));
                serviceAccount = new FileInputStream(resource.getFile());
                options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
            } catch (IOException e) {
                // Fallback to default classpath resource
                ClassPathResource defaultResource = new ClassPathResource("firebase-service-account.json");
                serviceAccount = new FileInputStream(defaultResource.getFile());
                options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
            }
        }

        // Inicializar solo si no existe ya
        if (FirebaseApp.getApps().isEmpty() && serviceAccount != null && options != null) {
            FirebaseApp.initializeApp(options);
        }

        return FirebaseApp.getInstance();
    }

    /**     * Enviar notificación simple a un solo dispositivo
     */
    public String sendToDevice(String token, String title, String body) throws Exception {
        // Esta método se usa desde FcmServiceImpl, pero en producción
        // preferimos usar firebaseApp() + FirebaseMessaging.getInstance().send()
        // Por ahora lanzamos una excepción para indicar que debe usarse la vía directa
        throw new Exception("Use FirebaseMessaging.getInstance().send() directly");
    }

    /**     * Enviar notificación en lote a múltiples dispositivos
     */
    public List<String> sendToMultipleDevices(List<String> tokens, String title, String body) throws Exception {
        List<String> successTokens = new ArrayList<>();
        for (String token : tokens) {
            try {
                sendToDevice(token, title, body);
                successTokens.add(token);
            } catch (Exception e) {
                // Token failed, skip it
            }
        }
        return successTokens;
    }
}