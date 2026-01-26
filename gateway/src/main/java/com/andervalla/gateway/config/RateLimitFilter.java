package com.andervalla.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Filtro de limitación de tasa (rate limiting) en el borde del Gateway (20 solicitudes por minuto por IP).
 * <p>
 * <strong>Propósito de seguridad:</strong> mitiga patrones de tráfico abusivo (ráfagas, fuerza bruta, scraping)
 * lo más temprano posible, devolviendo HTTP 429 sin invocar servicios aguas abajo.
 * <p>
 * <strong>Propósito de ahorro de costes (Azure):</strong> al rechazar exceso de solicitudes en la capa de Gateway,
 * se evita consumir CPU, hilos y red en los microservicios. Esto reduce la probabilidad de escalado automático
 * innecesario y ayuda a mantener el consumo de cómputo (y por tanto el coste) al mínimo.
 * <p>
 * Nota operativa: se excluyen probes de salud y solicitudes CORS de tipo preflight para evitar auto-bloqueos
 * accidentales (por ejemplo, que los health checks de la plataforma queden limitados).
 * <p>
 * Nota técnica: este módulo usa Spring Cloud Gateway <em>Server WebMVC</em> (stack Servlet), por lo que el punto
 * de integración correcto es un {@link jakarta.servlet.Filter} (vía {@link OncePerRequestFilter}), no un filtro
 * reactivo {@code GlobalFilter}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int REQUESTS_PER_MINUTE = 20;

    private static final long EVICT_AFTER_IDLE_MILLIS = Duration.ofMinutes(15).toMillis();
    private static final long EVICTION_EVERY_N_REQUESTS = 1024;

    private final ConcurrentHashMap<String, BucketEntry> bucketsByIp = new ConcurrentHashMap<>();

    private final AtomicLong requestCounter = new AtomicLong(0);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
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

        ConsumptionProbe probe = entry.bucket().tryConsumeAndReturnRemaining(1);

        response.setHeader("X-RateLimit-Limit", String.valueOf(REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long nanosToWait = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = Math.max(1L, (nanosToWait + 999_999_999L) / 1_000_000_000L);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        String requestId = MDC.get("requestId");
        log.warn(
                "action=rate_limit.block outcome=denied method={} path={} clientIp={} requestId={} limitPerMinute={} retryAfterSec={}",
                request.getMethod(),
                request.getRequestURI(),
                clientIp,
                requestId,
                REQUESTS_PER_MINUTE,
                retryAfterSeconds
        );

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"rate_limited\",\"message\":\"Too many requests\"}");
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
