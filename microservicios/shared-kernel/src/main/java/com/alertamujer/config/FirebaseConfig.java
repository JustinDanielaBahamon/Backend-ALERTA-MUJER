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

/** Used by Auth (Google sign-in) and Alert (push); Evidence turns it off with firebase.enabled=false. */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
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

        // Initialize only if it does not exist yet
        if (FirebaseApp.getApps().isEmpty() && serviceAccount != null && options != null) {
            FirebaseApp.initializeApp(options);
        }

        return FirebaseApp.getInstance();
    }

    /**
     * Send a simple notification to a single device
     */
    public String sendToDevice(String token, String title, String body) throws Exception {
        // This method is used from FcmServiceImpl, but in production
        // preferimos usar firebaseApp() + FirebaseMessaging.getInstance().send()
        // For now we throw an exception to signal that the direct path must be used
        throw new Exception("Use FirebaseMessaging.getInstance().send() directly");
    }

    /**
     * Send a batch notification to several devices
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