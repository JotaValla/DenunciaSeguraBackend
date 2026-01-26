package com.andervalla.msevidencias.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de correlación para trazabilidad de logs.
 * <p>
 * Lee {@code X-Request-Id} / {@code X-Correlation-Id} (si existe), o genera un UUID.
 * Publica el valor en {@link MDC} como {@code requestId} y lo expone en la respuesta como {@code X-Request-Id}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
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

    private static String resolveOrCreateRequestId(HttpServletRequest request) {
        String requestId = headerFirstNonBlank(request, "X-Request-Id", "X-Correlation-Id");
        if (requestId != null) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private static String headerFirstNonBlank(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
