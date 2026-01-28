package com.andervalla.msauth.services.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Service;

/**
 * Servicio para revocar refresh tokens y cerrar la sesión del servidor de autorización.
 */
@Service
public class LogoutService {

    private final OAuth2AuthorizationService authorizationService;
    private final String sessionCookieName;
    private final String sessionCookiePath;
    private final boolean sessionCookieSecure;
    private final boolean sessionCookieHttpOnly;
    private final String sessionCookieSameSite;

    public LogoutService(OAuth2AuthorizationService authorizationService,
                         @Value("${server.servlet.session.cookie.name:JSESSIONID}") String sessionCookieName,
                         @Value("${server.servlet.session.cookie.path:/}") String sessionCookiePath,
                         @Value("${server.servlet.session.cookie.secure:false}") boolean sessionCookieSecure,
                         @Value("${server.servlet.session.cookie.http-only:true}") boolean sessionCookieHttpOnly,
                         @Value("${server.servlet.session.cookie.same-site:Lax}") String sessionCookieSameSite) {
        this.authorizationService = authorizationService;
        this.sessionCookieName = sessionCookieName;
        this.sessionCookiePath = sessionCookiePath;
        this.sessionCookieSecure = sessionCookieSecure;
        this.sessionCookieHttpOnly = sessionCookieHttpOnly;
        this.sessionCookieSameSite = sessionCookieSameSite;
    }

    /**
     * Revoca el refresh token (si existe) eliminando la autorización asociada.
     */
    public boolean revocarRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        OAuth2Authorization authorization = authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);
        if (authorization == null) {
            return false;
        }
        authorizationService.remove(authorization);
        return true;
    }

    /**
     * Invalida la sesión HTTP y limpia la cookie de sesión del navegador.
     */
    public boolean invalidarSesion(HttpServletRequest request, HttpServletResponse response) {
        boolean invalidada = false;
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            invalidada = true;
        }

        ResponseCookie cookie = ResponseCookie.from(sessionCookieName, "")
                .path(sessionCookiePath)
                .maxAge(Duration.ZERO)
                .httpOnly(sessionCookieHttpOnly)
                .secure(sessionCookieSecure)
                .sameSite(sessionCookieSameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return invalidada;
    }
}
