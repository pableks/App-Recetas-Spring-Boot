package com.example.recetasfrontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow both frontend and backend origins
        config.addAllowedOrigin("http://localhost:8081"); // Frontend
        config.addAllowedOrigin("http://localhost:8080"); // Backend API
        
        // Allow all HTTP methods
        config.addAllowedMethod("*");
        
        // Allow all headers
        config.addAllowedHeader("*");
        
        // Allow credentials (important for session cookies)
        config.setAllowCredentials(true);
        
        // Expose headers that might be needed by the frontend
        config.addExposedHeader("Authorization");
        
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}