package com.example.RecetaApp.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.RecetaApp.dto.ApiResponse;
import com.example.RecetaApp.dto.LoginRequest;
import com.example.RecetaApp.dto.LoginResponse;
import com.example.RecetaApp.dto.UserDTO;
import com.example.RecetaApp.model.Usuario;
import com.example.RecetaApp.security.JwtUtils;
import com.example.RecetaApp.service.AuthService;
import com.example.RecetaApp.service.TokenBlacklistService;
import com.example.RecetaApp.service.UserSessionService;
import com.example.RecetaApp.service.UsuariosService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    // Define the logger at class level
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private static final String JWT_COOKIE_NAME = "AUTH-TOKEN";
    private static final String SESSION_ID_COOKIE = "SESSION-ID";
    private static final int COOKIE_MAX_AGE = 7200; // 2 hours in seconds

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserSessionService userSessionService;

    @PostConstruct
    public void init() {
        // Clear all sessions when server starts
        userSessionService.clearAllSessions();
        log.info("Cleared all sessions on server startup");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        String clientIP = extractClientIP(request);
        String username = loginRequest.getUsername();
        String userAgent = request.getHeader("User-Agent");
        LocalDateTime timestamp = LocalDateTime.now();
        
        try {
            log.info("Login attempt - IP: {} - Username: {} - Agent: {} - Time: {}", 
                clientIP, username, userAgent, timestamp.format(formatter));

            Usuario usuario = usuariosService.findByUsername(username);
            if (usuario == null) {
                log.warn("Login failed - User not found - IP: {} - Username: {} - Time: {}", 
                    clientIP, username, timestamp.format(formatter));
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Usuario no encontrado"));
            }

            // Always verify credentials regardless of existing session
            try {
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("Login failed - Invalid credentials - IP: {} - Username: {} - Time: {}", 
                    clientIP, username, timestamp.format(formatter));
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Credenciales inválidas"));
            }

            // If there's an existing session, invalidate it
            if (userSessionService.hasActiveSession(username, userAgent)) {
                log.info("Found existing session - Invalidating - IP: {} - Username: {} - Agent: {} - Time: {}", 
                    clientIP, username, userAgent, timestamp.format(formatter));
                String existingSessionId = userSessionService.getSessionId(username, userAgent);
                if (existingSessionId != null) {
                    userSessionService.removeSession(existingSessionId);
                }
            }

            // Generate new token and session
            String jwt = jwtUtils.generateJwtToken(SecurityContextHolder.getContext().getAuthentication());
            String sessionId = UUID.randomUUID().toString();
            
            Claims claims = jwtUtils.parseJwtToken(jwt);
            userSessionService.registerSession(sessionId, username, jwt, claims.getExpiration(), 
                                            userAgent, clientIP);

            // Add the cookies
            addTokenCookie(response, jwt);
            addSessionCookie(response, sessionId);

            log.info("Login successful - IP: {} - Username: {} - Agent: {} - Time: {}", 
                clientIP, username, userAgent, timestamp.format(formatter));

            return ResponseEntity.ok(new LoginResponse(true, 
                "Inicio de sesión exitoso", 
                new UserDTO(usuario)));

        } catch (Exception e) {
            log.error("Login failed - IP: {} - Username: {} - Time: {} - Error: {}", 
                clientIP, username, timestamp.format(formatter), e.getMessage(), e);
                
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, "Error en el inicio de sesión"));
        }
    }


    private String extractClientIP(HttpServletRequest request) {
        String clientIP = null;
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "REMOTE_ADDR"
        };
    
        for (String header : headers) {
            clientIP = request.getHeader(header);
            if (clientIP != null && !clientIP.isEmpty() && !"unknown".equalsIgnoreCase(clientIP)) {
                if (clientIP.contains(",")) {
                    clientIP = clientIP.split(",")[0].trim();
                }
                break;
            }
        }
    
        if (clientIP == null) {
            clientIP = request.getRemoteAddr();
        }
        
        log.debug("Extracted client IP: {} from request", clientIP);
        return clientIP;
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    private void addSessionCookie(HttpServletResponse response, String sessionId) {
        Cookie cookie = new Cookie(SESSION_ID_COOKIE, sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }



    @PostMapping("/logout")
public ResponseEntity<ApiResponse> logout(
        HttpServletRequest request,
        HttpServletResponse response) {
    try {
        Cookie[] cookies = request.getCookies();
        String token = null;
        String sessionId = null;
        String userAgent = request.getHeader("User-Agent");

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    token = cookie.getValue();
                } else if ("SESSION-ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }
        }

        // Log the logout attempt
        log.info("Logout attempt - Session ID: {}, User Agent: {}", sessionId, userAgent);

        boolean logoutSuccessful = false;

        if (token != null) {
            try {
                Claims claims = jwtUtils.parseJwtToken(token);
                String username = claims.getSubject();
                
                // Blacklist the token
                tokenBlacklistService.blacklistToken(token, claims.getExpiration());
                
                // Remove session if session ID is available
                if (sessionId != null) {
                    userSessionService.removeSession(sessionId);
                    log.debug("Removed session: {}", sessionId);
                }

                logoutSuccessful = true;
                log.info("Successfully logged out user: {}", username);
            } catch (Exception e) {
                log.warn("Error processing token during logout: {}", e.getMessage());
                // Continue with cookie cleanup even if token processing fails
            }
        }

        // Always clear cookies, even if token processing fails
        clearAuthCookies(response);
        
        // Clear security context
        SecurityContextHolder.clearContext();

        if (logoutSuccessful) {
            return ResponseEntity.ok(new ApiResponse(true, "Sesión cerrada exitosamente"));
        } else {
            // Still return success even if no active session was found
            return ResponseEntity.ok(new ApiResponse(true, "No se encontró sesión activa"));
        }

    } catch (Exception e) {
        log.error("Error during logout: ", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse(false, "Error al cerrar sesión"));
    }
}

private void clearAuthCookies(HttpServletResponse response) {
    // Clear JWT cookie
    Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(true);
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(0);
    response.addCookie(jwtCookie);

    // Clear session ID cookie
    Cookie sessionCookie = new Cookie("SESSION-ID", null);
    sessionCookie.setHttpOnly(true);
    sessionCookie.setSecure(true);
    sessionCookie.setPath("/");
    sessionCookie.setMaxAge(0);
    response.addCookie(sessionCookie);

    log.debug("Cleared auth cookies");
}

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        try {
            // Verificar autenticación (mantenemos tu verificación)
            if (!authService.isCurrentUserAuthenticated()) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "No hay sesión activa. Por favor, inicie sesión."));
            }

            Usuario currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(new ApiResponse(true, 
                "Usuario actual recuperado exitosamente", 
                new UserDTO(currentUser)));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al obtener usuario actual"));
        }
    }

    @GetMapping("/check-session")
public ResponseEntity<ApiResponse> checkSession(
        HttpServletRequest request,
        HttpServletResponse response) {
    try {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.debug("No cookies found in request");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "No hay sesión activa"));
        }

        String token = null;
        String sessionId = null;
        String userAgent = request.getHeader("User-Agent");

        for (Cookie cookie : cookies) {
            if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                token = cookie.getValue();
            } else if ("SESSION-ID".equals(cookie.getName())) {
                sessionId = cookie.getValue();
            }
        }

        if (token == null || sessionId == null) {
            log.debug("Missing required cookies");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "No hay sesión activa"));
        }

        try {
            Claims claims = jwtUtils.parseJwtToken(token);
            String username = claims.getSubject();

            if (!userSessionService.validateSession(sessionId, username, userAgent)) {
                log.debug("Invalid session for user: {}", username);
                clearAuthCookies(response);
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Sesión inválida"));
            }

            Usuario user = usuariosService.findByUsername(username);
            return ResponseEntity.ok(new ApiResponse(true, "Sesión activa", new UserDTO(user)));

        } catch (Exception e) {
            log.error("Error validating token", e);
            clearAuthCookies(response);
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Token inválido"));
        }

    } catch (Exception e) {
        log.error("Error checking session", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse(false, "Error al verificar sesión"));
    }
}

}