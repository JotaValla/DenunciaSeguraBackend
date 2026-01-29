package com.andervalla.gateway.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

/**
 * Resuelve el access token desde una cookie.
 */
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private final TokenCookieProperties cookieProperties;

    public CookieBearerTokenResolver(TokenCookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String cookieName = cookieProperties.getAccessTokenName();
        if (cookieName == null || cookieName.isBlank()) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
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
