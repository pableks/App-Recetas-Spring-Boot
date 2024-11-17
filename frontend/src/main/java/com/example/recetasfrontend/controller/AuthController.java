package com.example.recetasfrontend.controller;

import com.example.recetasfrontend.model.AuthResponse;
import com.example.recetasfrontend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);  // Add this line


    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            HttpServletRequest request,
            Model model) {
        
        if (isAuthenticated(request)) {
            return "redirect:/recetas";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpServletResponse response,
                       RedirectAttributes redirectAttributes) {
        try {
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Username and password are required");
                return "redirect:/auth/login?error";
            }

            AuthResponse authResponse = authService.login(username, password);
            
            if (authResponse.isSuccess() && 
                authResponse.getUser() != null && 
                authResponse.getAuthToken() != null) {
                
                Cookie authCookie = new Cookie("AUTH-TOKEN", authResponse.getAuthToken());
                authCookie.setPath("/");
                authCookie.setHttpOnly(true);
                authCookie.setMaxAge(7200); // 2 hours
                response.addCookie(authCookie);
                
                Cookie sessionCookie = new Cookie("SESSION-ID", authResponse.getSessionId());
                sessionCookie.setPath("/");
                sessionCookie.setHttpOnly(true);
                sessionCookie.setMaxAge(7200); // 2 hours
                response.addCookie(sessionCookie);
                
                return "redirect:/recetas";
            } else {
                String errorMessage = authResponse.getMessage() != null ? 
                    authResponse.getMessage() : "Invalid credentials";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/auth/login?error";
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/auth/login?error";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        log.info("Starting logout process");
        
        try {
            // Log request details
            log.info("Request URI: {}", request.getRequestURI());
            log.info("Request Method: {}", request.getMethod());
            
            // Log all request headers
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                log.info("Header {} : {}", headerName, request.getHeader(headerName));
            });

            // Log all cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    log.info("Cookie found - Name: {}, Value: {}, Path: {}", 
                        cookie.getName(), cookie.getValue(), cookie.getPath());
                }
            } else {
                log.warn("No cookies found in request");
            }

            // Call backend logout
            boolean logoutSuccess = authService.logout(request);
            log.info("Backend logout result: {}", logoutSuccess);

            // Clear frontend cookies regardless of backend response
            clearCookies(request, response);
            log.info("Frontend cookies cleared");

            if (logoutSuccess) {
                log.info("Logout successful");
                redirectAttributes.addFlashAttribute("message", "Successfully logged out");
            } else {
                log.warn("Backend logout failed but proceeding with frontend logout");
                redirectAttributes.addFlashAttribute("message", 
                    "Logged out locally. There might have been an issue with the server.");
            }

        } catch (Exception e) {
            log.error("Error during logout process", e);
            clearCookies(request, response);
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred during logout, but you have been logged out locally.");
        }

        return "redirect:/auth/login?logout";
    }

        

    private void clearCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTH-TOKEN".equals(cookie.getName()) || 
                    "SESSION-ID".equals(cookie.getName())) {
                    Cookie clearCookie = new Cookie(cookie.getName(), "");
                    clearCookie.setPath("/");
                    clearCookie.setMaxAge(0);
                    clearCookie.setHttpOnly(true);
                    clearCookie.setSecure(false); // Match your backend cookie settings
                    response.addCookie(clearCookie);
                    System.out.println("Cleared cookie: " + cookie.getName());
                }
            }
        }
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }

        boolean hasAuthToken = false;
        boolean hasSessionId = false;

        for (Cookie cookie : cookies) {
            if (cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                if ("AUTH-TOKEN".equals(cookie.getName())) {
                    hasAuthToken = true;
                } else if ("SESSION-ID".equals(cookie.getName())) {
                    hasSessionId = true;
                }
            }
        }

        return hasAuthToken && hasSessionId;
    }
}