package com.example.RecetaApp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.RecetaApp.service.CustomUserDetailsService;
import com.example.RecetaApp.service.TokenBlacklistService;
import com.example.RecetaApp.service.UserSessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String JWT_COOKIE_NAME = "AUTH-TOKEN";
    private static final String SESSION_ID_COOKIE = "SESSION-ID";

    // Exact matches for public paths
    private static final List<String> PROTECTED_PATHS = Arrays.asList(
        "/api/recetas/mis-recetas",
        "/api/recetas/actualizar",  
        "/api/recetas/crear",
        "/api/recetas/eliminar"
    );

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/auth/check-session",
        "/auth/logout",
        "/usuarios/register",
        "/error",
        "/swagger-ui",
        "/v3/api-docs",
        "/api/productos",
        "/api/recetas"
    );

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            
            log.debug("Processing {} request for path: {}", method, path);
            
            // First check if it's a protected path
            if (isProtectedPath(path)) {
                log.debug("Protected path accessed: {}", path);
                handleProtectedPath(request, response, filterChain);
                return;
            }

            // Then check if it's a public path
            if (isPublicPath(path)) {
                log.debug("Public path accessed: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // For all other paths, try to authenticate
            handleProtectedPath(request, response, filterChain);

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication error");
        }
    }

    private void handleProtectedPath(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws IOException, ServletException {
        String jwt = null;
        String sessionId = null;
        String userAgent = request.getHeader("User-Agent");

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                } else if (SESSION_ID_COOKIE.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }
        }

        if (jwt == null || sessionId == null) {
            log.debug("Missing required cookies for protected path");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication required");
            return;
        }

        if (!jwtUtils.validateJwtToken(jwt)) {
            log.debug("Invalid JWT token");
            clearAuthCookies(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        
        if (!userSessionService.validateSession(sessionId, username, userAgent)) {
            log.debug("Invalid session for user: {}", username);
            clearAuthCookies(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Session expired or invalid");
            return;
        }

        if (tokenBlacklistService.isBlacklisted(jwt)) {
            log.debug("Blacklisted token for user: {}", username);
            clearAuthCookies(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been invalidated");
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Set authentication for user: {} with authorities: {}", 
            username, userDetails.getAuthorities());

        userSessionService.updateLastActivity(sessionId);
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        // Update the check to handle path parameters
        return PROTECTED_PATHS.stream()
            .anyMatch(protectedPath -> 
                path.startsWith(protectedPath) || 
                path.matches(protectedPath.replace("/", "\\/") + "\\/\\d+")
            );
    }

    private boolean isPublicPath(String path) {
        // First check exact matches
        if (PUBLIC_PATHS.stream().anyMatch(path::equals)) {
            return true;
        }

        // Then check patterns for public endpoints
        return path.matches("/api/recetas/\\d+") || // Single recipe by ID
               path.matches("/api/recetas/dificultad/.*") || // Recipes by difficulty
               path.equals("/api/recetas"); // All recipes
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        
        Cookie sessionCookie = new Cookie(SESSION_ID_COOKIE, null);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        
        response.addCookie(jwtCookie);
        response.addCookie(sessionCookie);
    }
}