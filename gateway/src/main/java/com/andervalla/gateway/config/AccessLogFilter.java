package com.andervalla.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro de "access log" para el Gateway (stack Servlet).
 * <p>
 * Registra una línea por solicitud con un formato uniforme y fácil de leer (orientado a observabilidad).
 * Incluye campos operativos como método, ruta, estado HTTP, latencia, IP del cliente y un identificador de
 * correlación ({@code requestId}).
 * <p>
 * <strong>Correlación</strong>: si la solicitud trae {@code X-Request-Id} o {@code X-Correlation-Id}, se reutiliza;
 * si no, se genera un UUID. El valor se expone en la respuesta como {@code X-Request-Id} y se guarda en
 * {@link MDC} como {@code requestId} para que el resto de logs del mismo request lo incluyan automáticamente.
 * <p>
 * <strong>Buenas prácticas</strong>:
 * <ul>
 *   <li>Se excluyen preflight CORS ({@code OPTIONS}) para evitar ruido.</li>
 *   <li>Se excluye {@code /actuator} para no llenar logs con health checks.</li>
 *   <li>Se limita el tamaño del {@code User-Agent} para evitar logs inmanejables.</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger accessLog = LoggerFactory.getLogger("ACCESS");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNanos = System.nanoTime();

        String requestId = resolveOrCreateRequestId(request);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        HttpServletRequest requestForChain = new RequestIdHttpServletRequestWrapper(request, requestId);

        String clientIp = resolveClientIp(request);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String service = serviceHint(path);
        String userAgent = safeHeader(request, "User-Agent");

        try {
            filterChain.doFilter(requestForChain, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
            int status = response.getStatus();

            accessLog.info(
                    "action=http_request outcome=handled method={} path={} status={} durationMs={} service={} clientIp={} requestId={} userAgent={}",
                    method,
                    path,
                    status,
                    durationMs,
                    service,
                    clientIp,
                    requestId,
                    userAgent
            );

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

    /**
     * Obtiene un identificador de request desde headers comunes o genera uno nuevo.
     * <p>
     * Headers soportados (en orden): {@code X-Request-Id}, {@code X-Correlation-Id}.
     */
    private static String resolveOrCreateRequestId(HttpServletRequest request) {
        String requestId = headerFirstNonBlank(request, "X-Request-Id", "X-Correlation-Id");
        if (requestId != null) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Retorna el primer header no vacío dentro de la lista, o {@code null}.
     */
    private static String headerFirstNonBlank(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * Lee un header y devuelve un valor seguro para logging.
     * <p>
     * Normaliza a {@code "-"} cuando no existe y aplica un límite de longitud.
     */
    private static String safeHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            return "-";
        }
        String trimmed = value.trim();
        return trimmed.length() > 160 ? trimmed.substring(0, 160) : trimmed;
    }

    /**
     * Resuelve la IP del cliente priorizando {@code X-Forwarded-For} (primer hop) y
     * usando {@code request.getRemoteAddr()} como fallback.
     */
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

    /**
     * Heurística simple para etiquetar el "servicio afectado" en el access log.
     * <p>
     * No reemplaza al routeId real del Gateway; sirve como pista rápida para lectura de logs.
     */
    private static String serviceHint(String path) {
        if (path == null || path.isBlank()) {
            return "unknown";
        }

        if (path.startsWith("/auth")) {
            return "ms-auth";
        }
        if (path.startsWith("/usuarios") || path.startsWith("/interno/usuarios")) {
            return "ms-usuarios";
        }
        if (path.startsWith("/api/denuncias")) {
            return "ms-denuncias";
        }
        if (path.startsWith("/interno/adjuntar")
                || path.startsWith("/interno/entidades")
                || path.startsWith("/interno/uploads")
                || path.matches("^/interno/[^/]+/confirmar.*")) {
            return "ms-evidencias";
        }

        return "unknown";
    }

    private static final class RequestIdHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private static final String HEADER_REQUEST_ID = "X-Request-Id";
        private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

        private final String requestId;
        private final Set<String> headerNames;

        private RequestIdHttpServletRequestWrapper(HttpServletRequest request, String requestId) {
            super(request);
            this.requestId = requestId;

            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> existing = request.getHeaderNames();
            if (existing != null) {
                while (existing.hasMoreElements()) {
                    names.add(existing.nextElement());
                }
            }
            names.add(HEADER_REQUEST_ID);
            names.add(HEADER_CORRELATION_ID);
            this.headerNames = Collections.unmodifiableSet(names);
        }

        @Override
        public String getHeader(String name) {
            if (name == null) {
                return super.getHeader(null);
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            if (HEADER_REQUEST_ID.toLowerCase(Locale.ROOT).equals(normalized)
                    || HEADER_CORRELATION_ID.toLowerCase(Locale.ROOT).equals(normalized)) {
                return requestId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name == null) {
                return super.getHeaders(null);
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            if (HEADER_REQUEST_ID.toLowerCase(Locale.ROOT).equals(normalized)
                    || HEADER_CORRELATION_ID.toLowerCase(Locale.ROOT).equals(normalized)) {
                return Collections.enumeration(Collections.singletonList(requestId));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headerNames);
        }
    }
}
