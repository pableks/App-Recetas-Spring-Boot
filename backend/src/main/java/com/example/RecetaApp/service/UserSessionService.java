package com.example.RecetaApp.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Data;

@Service
public class UserSessionService {
    private static final Logger log = LoggerFactory.getLogger(UserSessionService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void init() {
        try {
            // Check if table exists
            boolean tableExists = jdbcTemplate.query(
                "SELECT COUNT(*) FROM user_tables WHERE table_name = 'USER_SESSIONS'",
                (rs, rowNum) -> rs.getInt(1)
            ).get(0) > 0;

            if (!tableExists) {
                // Updated table schema to include session ID and user agent
                jdbcTemplate.execute("""
                    CREATE TABLE user_sessions (
                        session_id VARCHAR2(255) PRIMARY KEY,
                        username VARCHAR2(255) NOT NULL,
                        token CLOB NOT NULL,
                        expiry_date TIMESTAMP NOT NULL,
                        user_agent VARCHAR2(500),
                        ip_address VARCHAR2(45),
                        last_activity TIMESTAMP,
                        CONSTRAINT uk_username_useragent UNIQUE (username, user_agent)
                    )
                """);
                
                // Create indexes
                jdbcTemplate.execute(
                    "CREATE INDEX idx_user_sessions_expiry ON user_sessions(expiry_date)"
                );
                jdbcTemplate.execute(
                    "CREATE INDEX idx_user_sessions_username ON user_sessions(username)"
                );
            }
        } catch (Exception e) {
            log.error("Error initializing user_sessions table: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public boolean hasActiveSession(String username, String userAgent) {
        try {
            return jdbcTemplate.query(
                """
                SELECT COUNT(*) 
                FROM user_sessions 
                WHERE username = ? 
                AND user_agent = ? 
                AND expiry_date > SYSTIMESTAMP
                """,
                (rs, rowNum) -> rs.getInt(1),
                username, userAgent
            ).get(0) > 0;
        } catch (Exception e) {
            log.error("Error checking active session for user: {} with agent: {}", username, userAgent, e);
            return false;
        }
    }

    @Transactional
    public void registerSession(String sessionId, String username, String token, Date expiryDate, 
                              String userAgent, String ipAddress) {
        try {
            // First, invalidate any existing session for this username + user agent combination
            jdbcTemplate.update(
                "DELETE FROM user_sessions WHERE username = ? AND user_agent = ?",
                username, userAgent
            );

            // Insert new session
            jdbcTemplate.update("""
                INSERT INTO user_sessions 
                (session_id, username, token, expiry_date, user_agent, ip_address, last_activity)
                VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
                """, 
                sessionId, username, token, 
                new java.sql.Timestamp(expiryDate.getTime()),
                userAgent, ipAddress
            );
            
            log.debug("Registered new session for user: {} with agent: {}", username, userAgent);
        } catch (Exception e) {
            log.error("Error registering session for user: {} with agent: {}", username, userAgent, e);
        }
    }

    @Transactional(readOnly = true)
    public Collection<String> getAllActiveTokens() {
        try {
            Set<String> tokens = new HashSet<>(jdbcTemplate.query(
                "SELECT token FROM user_sessions WHERE expiry_date > SYSTIMESTAMP",
                (rs, rowNum) -> rs.getString("token")
            ));
            log.debug("Retrieved {} active tokens", tokens.size());
            return tokens;
        } catch (Exception e) {
            log.error("Error getting all active tokens", e);
            return new HashSet<>();
        }
    }

    @Transactional
    public void clearAllSessions() {
        try {
            int count = jdbcTemplate.update("DELETE FROM user_sessions");
            log.info("Cleared {} active sessions", count);
        } catch (Exception e) {
            log.error("Error clearing all sessions", e);
        }
    }

    @Transactional(readOnly = true)
    public String getSessionId(String username, String userAgent) {
        try {
            return jdbcTemplate.query(
                """
                SELECT session_id 
                FROM user_sessions 
                WHERE username = ? 
                AND user_agent = ? 
                AND expiry_date > SYSTIMESTAMP
                """,
                (rs, rowNum) -> rs.getString("session_id"),
                username, userAgent
            ).stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.error("Error getting session ID for user: {} with agent: {}", username, userAgent, e);
            return null;
        }
    }

    @Transactional
    public void removeSession(String sessionId) {
        try {
            jdbcTemplate.update("DELETE FROM user_sessions WHERE session_id = ?", sessionId);
            log.debug("Removed session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error removing session: {}", sessionId, e);
        }
    }

    @Transactional(readOnly = true)
    public String getActiveToken(String username, String userAgent) {
        try {
            return jdbcTemplate.query(
                """
                SELECT token 
                FROM user_sessions 
                WHERE username = ? 
                AND user_agent = ? 
                AND expiry_date > SYSTIMESTAMP
                """,
                (rs, rowNum) -> rs.getString("token"),
                username, userAgent
            ).stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.error("Error getting active token for user: {} with agent: {}", username, userAgent, e);
            return null;
        }
    }

    @Transactional
    public void updateLastActivity(String sessionId) {
        try {
            jdbcTemplate.update(
                "UPDATE user_sessions SET last_activity = SYSTIMESTAMP WHERE session_id = ?",
                sessionId
            );
        } catch (Exception e) {
            log.error("Error updating last activity for session: {}", sessionId, e);
        }
    }

    @Transactional(readOnly = true)
    public boolean validateSession(String sessionId, String username, String userAgent) {
        try {
            return jdbcTemplate.query(
                """
                SELECT COUNT(*) 
                FROM user_sessions 
                WHERE session_id = ? 
                AND username = ? 
                AND user_agent = ? 
                AND expiry_date > SYSTIMESTAMP
                """,
                (rs, rowNum) -> rs.getInt(1),
                sessionId, username, userAgent
            ).get(0) > 0;
        } catch (Exception e) {
            log.error("Error validating session: {}", sessionId, e);
            return false;
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredSessions() {
        try {
            int count = jdbcTemplate.update(
                "DELETE FROM user_sessions WHERE expiry_date <= SYSTIMESTAMP"
            );
            if (count > 0) {
                log.info("Cleaned up {} expired sessions", count);
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions", e);
        }
    }

    @Transactional
    public void removeUserSessions(String username) {
        try {
            int count = jdbcTemplate.update(
                "DELETE FROM user_sessions WHERE username = ?",
                username
            );
            log.info("Removed {} sessions for user: {}", count, username);
        } catch (Exception e) {
            log.error("Error removing sessions for user: {}", username, e);
        }
    }
}