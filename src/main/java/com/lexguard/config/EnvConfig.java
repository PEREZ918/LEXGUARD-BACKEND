package com.lexguard.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;


@Configuration
public class EnvConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);

    @PostConstruct
    public void loadEnv() {
        try {
            
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .filename(".env")
                .load();

            
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                System.setProperty(key, value);
                
                
                if (isRedacted(key)) {
                    logger.info("Loaded env variable: {} = ***REDACTED***", key);
                } else {
                    logger.debug("Loaded env variable: {} = {}", key, value);
                }
            });

            logger.info("Environment variables loaded from .env file");

        } catch (Exception e) {
            logger.warn("Could not load .env file (this is expected in production): {}", e.getMessage());
        }
    }

    
    
    private boolean isRedacted(String key) {
        return key.toLowerCase().contains("password") ||
               key.toLowerCase().contains("secret") ||
               key.toLowerCase().contains("token") ||
               key.toLowerCase().contains("key");
    }
}
