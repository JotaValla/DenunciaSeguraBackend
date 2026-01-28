package com.andervalla.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades para leer el access token desde cookies.
 */
@ConfigurationProperties(prefix = "app.security.cookies")
public class TokenCookieProperties {

    /**
     * Nombre de la cookie con el access token.
     */
    private String accessTokenName = "DS_ACCESS_TOKEN";

    public String getAccessTokenName() {
        return accessTokenName;
    }

    public void setAccessTokenName(String accessTokenName) {
        this.accessTokenName = accessTokenName;
    }
}
