package com.andervalla.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Edge rate limiting filter (20 requests per minute per client IP) implemented in the Gateway.
 * <p>
 * <strong>Security purpose:</strong> blocks abusive traffic patterns (burst requests, brute force, scraping)
 * as early as possible, returning HTTP 429 without invoking downstream services.
 * <p>
 * <strong>Cost-saving purpose (Azure):</strong> by rejecting excess requests at the Gateway layer, this avoids
 * spending CPU, threads, and network on microservices. That reduces the probability of autoscaling events and
 * helps keep compute usage (and therefore cost) minimal.
 * <p>
 * Operational note: health probes and CORS preflight requests are excluded to avoid accidental self-denial of
 * service (e.g., platform health checks being rate-limited).
 * <p>
 * Note: this Gateway module uses Spring Cloud Gateway <em>Server WebMVC</em> (Servlet stack), so the correct
 * integration point is a Servlet {@link jakarta.servlet.Filter} (via {@link OncePerRequestFilter}), not a
 * reactive {@code GlobalFilter}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

    private static final int REQUESTS_PER_MINUTE = 20;

    private static final long EVICT_AFTER_IDLE_MILLIS = Duration.ofMinutes(15).toMillis();
    private static final long EVICTION_EVERY_N_REQUESTS = 1024;

    private final ConcurrentHashMap<String, BucketEntry> bucketsByIp = new ConcurrentHashMap<>();

    private final AtomicLong requestCounter = new AtomicLong(0);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        BucketEntry entry = bucketsByIp.computeIfAbsent(clientIp, ignored -> new BucketEntry(newBucket()));
        entry.touch();

        maybeEvictOldEntries();

        if (entry.bucket().tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.flushBuffer();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        if (method != null && method.equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        String path = request.getRequestURI();
        return path != null && path.startsWith("/actuator");
    }

    private static Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(
                REQUESTS_PER_MINUTE,
                Refill.intervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String first = xForwardedFor.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }

    private void maybeEvictOldEntries() {
        long current = requestCounter.incrementAndGet();
        if (current % EVICTION_EVERY_N_REQUESTS != 0) {
            return;
        }

        long cutoff = System.currentTimeMillis() - EVICT_AFTER_IDLE_MILLIS;
        for (var entry : bucketsByIp.entrySet()) {
            if (entry.getValue().lastAccessMillisValue() < cutoff) {
                bucketsByIp.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private record BucketEntry(Bucket bucket, AtomicLong lastAccessMillis) {
        BucketEntry(Bucket bucket) {
            this(bucket, new AtomicLong(System.currentTimeMillis()));
        }

        void touch() {
            lastAccessMillis.set(System.currentTimeMillis());
        }

        long lastAccessMillisValue() {
            return lastAccessMillis.get();
        }
    }
}
