package com.example.RecetaApp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.RecetaApp.security.RateLimitInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**", "/auth/**", "/usuarios/**") // Add your path patterns
            .excludePathPatterns("/error", "/swagger-ui/**", "/v3/api-docs/**"); // Exclude paths if needed
    }
}