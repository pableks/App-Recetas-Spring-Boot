package com.example.RecetaApp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.example.RecetaApp.service.TokenBlacklistService;
import com.example.RecetaApp.service.UserSessionService;

import java.util.Collection;

@Component
public class AuthControllerStartup implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(AuthControllerStartup.class);
    
    @Autowired
    private UserSessionService userSessionService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application startup - Invalidating all existing sessions");
        
        try {
            // Get all active tokens before clearing sessions
            Collection<String> activeTokens = userSessionService.getAllActiveTokens();
            
            // Clear all sessions
            userSessionService.clearAllSessions();
            
            // Blacklist all previously active tokens
            for (String token : activeTokens) {
                try {
                    tokenBlacklistService.blacklistToken(token, null);
                } catch (Exception e) {
                    log.error("Error blacklisting token during startup: {}", e.getMessage(), e);
                }
            }
            
            log.info("Successfully invalidated {} existing sessions on startup", activeTokens.size());
            
        } catch (Exception e) {
            log.error("Error during startup session invalidation: {}", e.getMessage(), e);
        }
    }
}