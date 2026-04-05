package com.FDPACB.FDPACB.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limiting interceptor using the Token Bucket algorithm (Bucket4j).
 * <p>
 * Limits each client to 50 requests per minute. The client is identified by:
 * <ul>
 *     <li>The authenticated user's JWT subject (username) if present</li>
 *     <li>The remote IP address otherwise</li>
 * </ul>
 * Returns HTTP 429 (Too Many Requests) with a Retry-After header when the limit is exceeded.
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 50;

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        String clientKey = resolveClientKey(request);
        Bucket bucket = bucketCache.computeIfAbsent(clientKey, this::createBucket);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("X-Rate-Limit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            return true;
        }

        // Rate limit exceeded
        long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {
                  "status": 429,
                  "message": "Rate limit exceeded. Please wait %d seconds before retrying.",
                  "errors": null,
                  "timestamp": "%s"
                }
                """.formatted(waitSeconds, java.time.LocalDateTime.now()));
        return false;
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_REQUESTS_PER_MINUTE)
                .refillGreedy(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        // Prefer authenticated username from the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract a short key from the token (the subject is in the payload)
            // We use the token's last 20 characters as a fast key (avoids parsing JWT again)
            String token = authHeader.substring(7);
            return "token:" + token.substring(Math.max(0, token.length() - 20));
        }
        // Fallback to IP address
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
