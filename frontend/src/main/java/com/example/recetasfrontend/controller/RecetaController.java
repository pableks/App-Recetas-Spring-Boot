package com.example.recetasfrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // Changed from @RestController
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
  // Add this for development. For production, specify exact origins

public class RecetaController {
    
    @GetMapping("/recetas")  // Changed from @RequestMapping
    public String viewRecetas() {
        return "recetas";  // This will look for recetas.html in src/main/resources/templates/
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";  // Will render login.html
    }
}