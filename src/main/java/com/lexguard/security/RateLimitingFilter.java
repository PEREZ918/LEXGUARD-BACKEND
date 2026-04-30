package com.lexguard.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    
    private static final int AUTH_LIMIT_PER_MINUTE = 10;
    private static final int GENERAL_LIMIT_PER_MINUTE = 100;
    private static final long MINUTE_IN_MS = 60000;

    
    private final Map<String, ClientBucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, ClientBucket> generalBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIP = getClientIP(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        
        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAuthEndpoint = path.startsWith("/api/auth/");
        Map<String, ClientBucket> buckets = isAuthEndpoint ? authBuckets : generalBuckets;
        int limit = isAuthEndpoint ? AUTH_LIMIT_PER_MINUTE : GENERAL_LIMIT_PER_MINUTE;

        
        ClientBucket bucket = buckets.computeIfAbsent(clientIP, k -> new ClientBucket(limit));

        
        if (bucket.allow()) {
            
            filterChain.doFilter(request, response);
        } else {
            
            String type = isAuthEndpoint ? "auth" : "general";
            logger.warn("Rate limit exceeded for IP {} on {} endpoint (path: {})", clientIP, type, path);

            response.setStatus(429); 
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Demasiados intentos. Intenta de nuevo en 1 minuto.\"}");
            response.getWriter().flush();
        }
    }

    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    
    private static class ClientBucket {
        private int tokensRemaining;
        private long lastRefillTime;
        private final int capacity;

        ClientBucket(int capacity) {
            this.capacity = capacity;
            this.tokensRemaining = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean allow() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;

            
            if (timePassed > MINUTE_IN_MS) {
                tokensRemaining = capacity;
                lastRefillTime = now;
            }

            
            if (tokensRemaining > 0) {
                tokensRemaining--;
                return true;
            }

            return false;
        }
    }
}
