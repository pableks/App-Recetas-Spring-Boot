package com.example.RecetaApp.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Value("${rate.limit.requests-per-minute:300}")
    private int requestsPerMinute;

    private Bucket createNewBucket() {
        Refill refill = Refill.intervally(requestsPerMinute, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // First try to get authenticated user
        String username = request.getUserPrincipal() != null ? 
            request.getUserPrincipal().getName() : null;
            
        if (username != null) {
            return "user:" + username;
        }

        // Fall back to IP address
        String clientIP = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            clientIP = forwardedFor.split(",")[0].trim();
        }

        log.debug("Rate limit client identifier: {}", clientIP);
        return "ip:" + clientIP;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        String clientId = getClientIdentifier(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> createNewBucket());
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Add headers to show rate limit info
            response.addHeader("X-Rate-Limit-Remaining", 
                String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Limit", 
                String.valueOf(requestsPerMinute));
            return true;
        } else {
            long waitTime = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", 
                String.valueOf(waitTime));
            response.getWriter().write(
                String.format("Too many requests - please wait %d seconds", waitTime));
            return false;
        }
    }
}