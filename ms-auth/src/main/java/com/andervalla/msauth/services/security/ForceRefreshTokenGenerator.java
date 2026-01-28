package com.andervalla.msauth.services.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

/**
 * Genera refresh tokens para flujos Authorization Code + PKCE cuando el cliente lo permite.
 */
@Component
public class ForceRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {

    private static final String OFFLINE_ACCESS_SCOPE = "offline_access";

    /**
     * Emite refresh tokens si el grant es Authorization Code/Refresh Token y el cliente lo permite.
     */
    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        if (context == null || context.getTokenType() == null) {
            return null;
        }
        if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        }
        if (context.getRegisteredClient() == null) {
            return null;
        }
        if (!context.getRegisteredClient().getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            return null;
        }
        AuthorizationGrantType grantType = context.getAuthorizationGrantType();
        if (grantType == null) {
            return null;
        }
        if (!AuthorizationGrantType.AUTHORIZATION_CODE.equals(grantType)
                && !AuthorizationGrantType.REFRESH_TOKEN.equals(grantType)) {
            return null;
        }

        Set<String> scopes = context.getAuthorizedScopes();
        if (scopes != null && !scopes.isEmpty() && !scopes.contains(OFFLINE_ACCESS_SCOPE)) {
            return null;
        }

        Instant issuedAt = Instant.now();
        Duration ttl = context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive();
        Instant expiresAt = ttl != null ? issuedAt.plus(ttl) : null;
        return new OAuth2RefreshToken(UUID.randomUUID().toString(), issuedAt, expiresAt);
    }
}
