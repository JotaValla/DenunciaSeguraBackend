package com.andervalla.msauth.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Escribe la respuesta del token y emite cookies HttpOnly con access/refresh tokens.
 */
public class TokenCookieAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AccessTokenResponseHttpMessageConverter tokenResponseConverter =
            new OAuth2AccessTokenResponseHttpMessageConverter();
    private final TokenCookieProperties cookieProperties;
    private final AuthenticationEntryPoint errorEntryPoint;

    public TokenCookieAuthenticationSuccessHandler(TokenCookieProperties cookieProperties,
                                                   AuthenticationEntryPoint errorEntryPoint) {
        this.cookieProperties = cookieProperties;
        this.errorEntryPoint = errorEntryPoint;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AccessTokenAuthenticationToken tokenAuthentication)) {
            AuthenticationException ex = new org.springframework.security.authentication.AuthenticationServiceException(
                    "Unexpected authentication type: " + authentication.getClass().getName());
            errorEntryPoint.commence(request, response, ex);
            return;
        }

        OAuth2AccessToken accessToken = tokenAuthentication.getAccessToken();
        OAuth2RefreshToken refreshToken = tokenAuthentication.getRefreshToken();

        if (cookieProperties.isEnabled()) {
            addTokenCookie(response, cookieProperties.getAccessTokenName(), accessToken.getTokenValue(),
                    accessToken.getIssuedAt(), accessToken.getExpiresAt());
            if (refreshToken != null) {
                addTokenCookie(response, cookieProperties.getRefreshTokenName(), refreshToken.getTokenValue(),
                        refreshToken.getIssuedAt(), refreshToken.getExpiresAt());
            }
        }

        OAuth2AccessTokenResponse tokenResponse = buildTokenResponse(accessToken, refreshToken,
                tokenAuthentication.getAdditionalParameters());

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        tokenResponseConverter.write(tokenResponse, null, new ServletServerHttpResponse(response));
    }

    private OAuth2AccessTokenResponse buildTokenResponse(OAuth2AccessToken accessToken,
                                                         OAuth2RefreshToken refreshToken,
                                                         Map<String, Object> additionalParameters) {
        long expiresIn = 0L;
        if (accessToken.getIssuedAt() != null && accessToken.getExpiresAt() != null) {
            expiresIn = Duration.between(accessToken.getIssuedAt(), accessToken.getExpiresAt()).getSeconds();
        }
        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
                .tokenType(accessToken.getTokenType())
                .scopes(accessToken.getScopes());
        if (expiresIn > 0) {
            builder.expiresIn(expiresIn);
        }
        if (refreshToken != null) {
            builder.refreshToken(refreshToken.getTokenValue());
        }
        if (additionalParameters != null && !additionalParameters.isEmpty()) {
            builder.additionalParameters(additionalParameters);
        }
        return builder.build();
    }

    private void addTokenCookie(HttpServletResponse response,
                                String name,
                                String value,
                                Instant issuedAt,
                                Instant expiresAt) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path(cookieProperties.getPath())
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite());
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        if (issuedAt != null && expiresAt != null && expiresAt.isAfter(issuedAt)) {
            builder.maxAge(Duration.between(issuedAt, expiresAt));
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
