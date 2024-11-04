package com.example.RecetaApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.RecetaApp.security.JwtUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    private Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());
    private Map<String, Long> tokenExpirations = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private JwtUtils jwtUtils;

    // Constructor to register shutdown hook
    public TokenBlacklistService() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Server shutting down - Invalidating all tokens");
            invalidateAllTokens();
        }));
    }

    @jakarta.annotation.PreDestroy
    public void onShutdown() {
        log.info("Application shutting down - Invalidating all active tokens");
        invalidateAllTokens();
    }

    public void invalidateAllTokens() {
        try {
            // Get all active tokens from UserSessionService
            Collection<String> activeTokens = userSessionService.getAllActiveTokens();
            
            // Add all active tokens to blacklist
            for (String token : activeTokens) {
                try {
                    Date expiryDate = jwtUtils.getExpirationDateFromJwtToken(token);
                    blacklistToken(token, expiryDate);
                    log.debug("Token blacklisted during shutdown: {}", maskToken(token));
                } catch (Exception e) {
                    log.error("Error blacklisting token during shutdown", e);
                }
            }

            // Clear all active sessions
            userSessionService.clearAllSessions();
            
            log.info("Successfully invalidated {} tokens during shutdown", activeTokens.size());
        } catch (Exception e) {
            log.error("Error during token invalidation on shutdown", e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        log.debug("Starting scheduled cleanup of expired tokens");
        long now = System.currentTimeMillis();
        int removedCount = 0;
        
        synchronized (tokenExpirations) {
            Iterator<Map.Entry<String, Long>> iterator = tokenExpirations.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                if (entry.getValue() < now) {
                    blacklistedTokens.remove(entry.getKey());
                    iterator.remove();
                    removedCount++;
                }
            }
        }
        
        log.debug("Cleaned up {} expired tokens", removedCount);
    }

    public void blacklistToken(String token, Date expiryDate) {
        if (token == null || expiryDate == null) {
            log.warn("Attempted to blacklist token with null values");
            return;
        }
        
        blacklistedTokens.add(token);
        tokenExpirations.put(token, expiryDate.getTime());
        log.debug("Token blacklisted: {}", maskToken(token));
    }

    public boolean isBlacklisted(String token) {
        if (token == null) {
            log.warn("Null token checked against blacklist");
            return true;
        }
        return blacklistedTokens.contains(token);
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "INVALID_TOKEN";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}