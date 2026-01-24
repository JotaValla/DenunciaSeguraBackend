package com.andervalla.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class GatewayLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !(uri.startsWith("/auth/oauth2/authorize")
                || uri.startsWith("/auth/login")
                || uri.startsWith("/oauth2/authorize")
                || uri.startsWith("/login")
                || uri.startsWith("/auth/callback"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String cookies = request.getHeader("Cookie");
        log.info("GatewayLogging[req] uri={} query={} sessionId={} cookies={}",
                request.getRequestURI(),
                request.getQueryString(),
                session != null ? session.getId() : "none",
                cookies != null ? cookies : "none");
        filterChain.doFilter(request, response);
        HttpSession sessionAfter = request.getSession(false);
        Collection<String> setCookies = response.getHeaders("Set-Cookie");
        if (setCookies != null && !setCookies.isEmpty()) {
            log.info("GatewayLogging[res] uri={} sessionId={} set-cookie={}",
                    request.getRequestURI(),
                    sessionAfter != null ? sessionAfter.getId() : "none",
                    setCookies);
        }
    }
}
