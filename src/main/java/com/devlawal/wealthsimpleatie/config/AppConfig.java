package com.devlawal.wealthsimpleatie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enables binding of {@link AtieProperties} and global CORS configuration.
 *
 * CORS policy (MVP — no auth yet):
 *  - Origins are configurable via atie.cors.allowed-origins (comma-separated)
 *  - Defaults cover the two standard Vite/CRA dev ports
 *  - All standard HTTP methods and headers are allowed
 *  - When auth is added, replace * credentials handling here in one place
 */
@Configuration
@EnableConfigurationProperties(AtieProperties.class)
public class AppConfig {

    /**
     * Configures CORS globally for all /api/** routes.
     * Allowed origins are driven by the atie.cors.allowed-origins property so
     * the deployed URL can be set via environment variable without rebuilding.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer(
            @Value("${atie.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
            String[] allowedOrigins) {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600); // browser caches preflight for 1 hour
            }
        };
    }
}
