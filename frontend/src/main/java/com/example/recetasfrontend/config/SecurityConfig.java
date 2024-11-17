package com.example.recetasfrontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for simplicity
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/recetas",
                    "/recetas/view/**",
                    "/auth/**",
                    "/recetas/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/error",
                    "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .disable()  // Disable Spring's default login form
            )
            .logout(logout -> logout
                .disable()  // Disable Spring's default logout handling
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}