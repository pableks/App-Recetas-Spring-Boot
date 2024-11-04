package com.example.RecetaApp.service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ApplicationShutdownHandler implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ApplicationShutdownHandler.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public void destroy() throws Exception {
        log.info("Application shutdown initiated - cleaning up sessions and tokens");
        
        try {
            // Get all active tokens before clearing sessions
            var activeTokens = userSessionService.getAllActiveTokens();
            
            // Blacklist all active tokens
            if (!activeTokens.isEmpty()) {
                for (String token : activeTokens) {
                    try {
                        // Since the application is shutting down, we'll set a far future expiration
                        // This ensures tokens stay invalid even after restart
                        tokenBlacklistService.blacklistToken(token, 
                            new java.util.Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)); // 1 year
                    } catch (Exception e) {
                        log.error("Error blacklisting token during shutdown", e);
                    }
                }
                log.info("Successfully blacklisted {} active tokens", activeTokens.size());
            }

            // Clear all active sessions
            userSessionService.clearAllSessions();
            log.info("Successfully cleared all active sessions");
            
        } catch (Exception e) {
            log.error("Error during application shutdown cleanup", e);
            throw e; // Rethrow to ensure Spring is aware of the failure
        }
    }
}