package com.andervalla.msauth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Permite tomar el refresh_token desde cookie cuando se llama a /oauth2/token.
 */
public class RefreshTokenCookieFilter extends OncePerRequestFilter {

    private final TokenCookieProperties cookieProperties;

    public RefreshTokenCookieFilter(TokenCookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/oauth2/token".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"refresh_token".equals(request.getParameter("grant_type"))
                || StringUtils.hasText(request.getParameter("refresh_token"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = extractCookieValue(request, cookieProperties.getRefreshTokenName());
        if (!StringUtils.hasText(refreshToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if ("refresh_token".equals(name)) {
                    return refreshToken;
                }
                return super.getParameter(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> params = new HashMap<>(super.getParameterMap());
                params.put("refresh_token", new String[]{refreshToken});
                return params;
            }
        };

        filterChain.doFilter(wrapper, response);
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null || cookieName.isBlank()) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
