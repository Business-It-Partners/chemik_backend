package com.chemiki.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing Firebase with config file: {}", firebaseConfigPath);

            // Check if Firebase is already initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized. Apps count: {}", FirebaseApp.getApps().size());
                return;
            }

            // Load credentials from classpath
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            if (!resource.exists()) {
                throw new RuntimeException("Firebase config file not found: " + firebaseConfigPath);
            }

            log.info("Loading Firebase credentials from: {}", resource.getURI());

            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully. App name: {}, Project ID: {}",
                    app.getName(), app.getOptions().getProjectId());

        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Firebase initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during Firebase initialization: " + e.getMessage(), e);
        }
    }
}