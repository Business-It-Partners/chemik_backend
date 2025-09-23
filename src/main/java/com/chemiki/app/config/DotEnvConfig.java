package com.chemiki.app.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Slf4j
@Configuration
public class DotEnvConfig {

    static {
        try {
            // Load .env file from project root
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // Look in project root
                    .ignoreIfMissing() // Don't fail if .env doesn't exist
                    .load();

            // Set system properties so Spring can access them
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();

                // Only set if not already set (allows override via system properties)
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                    log.debug("Loaded from .env: {} = {}", key,
                            key.contains("PASSWORD") || key.contains("SECRET") ? "***" : value);
                }
            });

            log.info("✅ .env file loaded successfully from project root");

        } catch (Exception e) {
            log.warn("⚠️ Could not load .env file: {}", e.getMessage());
            log.info("Continuing with system environment variables and application.yml defaults");
        }
    }

    @PostConstruct
    public void init() {
        // Check if key variables are loaded
        String jwtSecret = System.getProperty("JWT_SECRET");
        String dbUrl = System.getProperty("DB_URL");

        if (jwtSecret != null) {
            log.info("✅ JWT_SECRET loaded from environment");
        } else {
            log.warn("⚠️ JWT_SECRET not found in environment, using application.yml default");
        }

        if (dbUrl != null) {
            log.info("✅ DB_URL loaded: {}", dbUrl);
        } else {
            log.warn("⚠️ DB_URL not found in environment, using application.yml default");
        }
    }
}