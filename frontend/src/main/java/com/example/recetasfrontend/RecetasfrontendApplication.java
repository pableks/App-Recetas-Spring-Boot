package com.example.recetasfrontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class RecetasfrontendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecetasfrontendApplication.class, args);
    }
}