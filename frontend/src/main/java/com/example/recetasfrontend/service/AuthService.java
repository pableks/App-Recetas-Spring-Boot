package com.example.recetasfrontend.service;

import com.example.recetasfrontend.controller.AuthController;
import com.example.recetasfrontend.model.AuthRequest;
import com.example.recetasfrontend.model.AuthResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;                  // Add this import
import org.slf4j.LoggerFactory;           // Add this import
import java.util.Collections;

@Service
public class AuthService {
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);  // Add this line


    @Autowired
    public AuthService(
        RestTemplate restTemplate,
        @Value("${api.base.url}") String apiBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    public AuthResponse login(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            AuthRequest authRequest = new AuthRequest(username, password);
            HttpEntity<AuthRequest> request = new HttpEntity<>(authRequest, headers);

            String loginUrl = apiBaseUrl + "/auth/login";
            System.out.println("Attempting login at: " + loginUrl);

            ResponseEntity<AuthResponse> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                request,
                AuthResponse.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Authentication failed");
                return errorResponse;
            }

            AuthResponse authResponse = response.getBody();
            if (authResponse == null || authResponse.getUser() == null || !authResponse.isSuccess()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Invalid credentials or server error");
                return errorResponse;
            }
            
            // Set auth token and session ID from response headers if available
            HttpHeaders responseHeaders = response.getHeaders();
            if (responseHeaders.containsKey(HttpHeaders.SET_COOKIE)) {
                String authToken = null;
                String sessionId = null;
                
                for (String cookie : responseHeaders.get(HttpHeaders.SET_COOKIE)) {
                    System.out.println("Received cookie: " + cookie);  // Debug log
                    
                    if (cookie.contains("AUTH-TOKEN=")) {
                        authToken = extractCookieValue(cookie, "AUTH-TOKEN=");
                    } else if (cookie.contains("SESSION-ID=")) {
                        sessionId = extractCookieValue(cookie, "SESSION-ID=");
                    }
                }

                if (authToken != null) {
                    authResponse.setAuthToken(authToken);
                }
                if (sessionId != null) {
                    authResponse.setSessionId(sessionId);
                }

                // Only consider it successful if we have both tokens
                authResponse.setSuccess(authToken != null && sessionId != null);
                
                System.out.println("Extracted AUTH-TOKEN: " + authToken);  // Debug log
                System.out.println("Extracted SESSION-ID: " + sessionId);  // Debug log
            } else {
                System.out.println("No SET-COOKIE headers found in response");  // Debug log
            }

            return authResponse;

        } catch (HttpClientErrorException.Unauthorized e) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setSuccess(false);
            authResponse.setMessage("Invalid credentials");
            return authResponse;
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            AuthResponse authResponse = new AuthResponse();
            authResponse.setSuccess(false);
            authResponse.setMessage("Login failed: " + e.getMessage());
            return authResponse;
        }
    }

    public boolean logout(HttpServletRequest request) {
        try {
            String logoutUrl = apiBaseUrl + "/auth/logout";
            
            // Get cookies from request
            Cookie[] cookies = request.getCookies();
            String authToken = null;
            String sessionId = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("AUTH-TOKEN".equals(cookie.getName())) {
                        authToken = cookie.getValue();
                    } else if ("SESSION-ID".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                    }
                }
            }

            if (authToken == null || sessionId == null) {
                log.debug("Missing required cookies for logout - authToken: {}, sessionId: {}", 
                    authToken != null ? "present" : "null", 
                    sessionId != null ? "present" : "null");
                return false;
            }

            // Create headers with all necessary information
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Set cookies as actual Cookie header
            headers.add("Cookie", String.format("AUTH-TOKEN=%s; SESSION-ID=%s", authToken, sessionId));
            
            // Log the request details
            log.debug("Sending logout request to: {} with headers: {}", logoutUrl, headers);

            HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                requestEntity,
                Void.class
            );
            
            log.debug("Logout response status: {}", response.getStatusCode());
            
            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("Logout error", e);
            return false;
        }
    }


    private String extractCookieValue(String cookie, String prefix) {
        int startIndex = cookie.indexOf(prefix) + prefix.length();
        if (startIndex < prefix.length()) return null;
        
        int endIndex = cookie.indexOf(';', startIndex);
        if (endIndex == -1) {
            endIndex = cookie.length();
        }
        
        String value = cookie.substring(startIndex, endIndex).trim();
        return value.isEmpty() ? null : value;
    }
}