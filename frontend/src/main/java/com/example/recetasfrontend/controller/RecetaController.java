package com.example.recetasfrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Controller
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class RecetaController {

    @GetMapping({"/", "/recetas"})
    public String viewRecetas(HttpServletRequest request, Model model) {
        addAuthAttributesToModel(request, model);
        return "recetas";
    }

    @GetMapping("/recetas/view/{id}")
    public String viewReceta(@PathVariable Long id, HttpServletRequest request, Model model) {
        addAuthAttributesToModel(request, model);
        model.addAttribute("recipeId", id);
        return "receta-detail";
    }

    @GetMapping("/recetas/create")
    public String createReceta(HttpServletRequest request, Model model) {
        // Debug logging
        System.out.println("Checking authentication for /recetas/create");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Cookie found: " + cookie.getName() + " = " + cookie.getValue());
            }
        }

        boolean authenticated = isAuthenticated(request);
        System.out.println("Is authenticated: " + authenticated);

        if (!authenticated) {
            return "redirect:/auth/login";
        }
        
        addAuthAttributesToModel(request, model);
        return "receta-form";
    }

    @GetMapping("/recetas/edit/{id}")
    public String editReceta(@PathVariable Long id, HttpServletRequest request, Model model) {
        if (!isAuthenticated(request)) {
            return "redirect:/auth/login";
        }
        addAuthAttributesToModel(request, model);
        model.addAttribute("recipeId", id);
        return "receta-edit";
    }

    @GetMapping("/auth/login")
    public String showLogin(HttpServletRequest request) {
        if (isAuthenticated(request)) {
            return "redirect:/recetas";
        }
        return "login";
    }

    @GetMapping("/recetas/my-recipes")
    public String myRecipes(HttpServletRequest request, Model model) {
        if (!isAuthenticated(request)) {
            return "redirect:/auth/login";
        }
        
        addAuthAttributesToModel(request, model);
        return "my-recipes";
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        if (request.getCookies() == null) {
            System.out.println("No cookies found");
            return false;
        }

        boolean hasAuthToken = false;
        boolean hasSessionId = false;

        for (Cookie cookie : request.getCookies()) {
            if ("AUTH-TOKEN".equals(cookie.getName()) && 
                cookie.getValue() != null && 
                !cookie.getValue().isEmpty()) {
                hasAuthToken = true;
            }
            if ("SESSION-ID".equals(cookie.getName()) && 
                cookie.getValue() != null && 
                !cookie.getValue().isEmpty()) {
                hasSessionId = true;
            }
        }

        // Debug logging
        System.out.println("Has AUTH-TOKEN: " + hasAuthToken);
        System.out.println("Has SESSION-ID: " + hasSessionId);

        // Consider authenticated if either AUTH-TOKEN or SESSION-ID is present
        return hasAuthToken || hasSessionId;
    }

    private void addAuthAttributesToModel(HttpServletRequest request, Model model) {
        boolean isAuthenticated = isAuthenticated(request);
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        if (isAuthenticated && request.getCookies() != null) {
            String authToken = null;
            String sessionId = null;
            
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH-TOKEN".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                } else if ("SESSION-ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }
            
            if (authToken != null) {
                model.addAttribute("authToken", authToken);
            }
            if (sessionId != null) {
                model.addAttribute("sessionId", sessionId);
            }
        }
    }
}