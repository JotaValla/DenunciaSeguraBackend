package com.andervalla.msauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades para emitir tokens en cookies HttpOnly.
 */
@ConfigurationProperties(prefix = "app.security.cookies")
public class TokenCookieProperties {

    /**
     * Habilita la emisión de cookies para access/refresh tokens.
     */
    private boolean enabled = true;

    /**
     * Nombre de la cookie que guarda el access token.
     */
    private String accessTokenName = "DS_ACCESS_TOKEN";

    /**
     * Nombre de la cookie que guarda el refresh token.
     */
    private String refreshTokenName = "DS_REFRESH_TOKEN";

    /**
     * Path de las cookies.
     */
    private String path = "/";

    /**
     * Domain opcional para las cookies (vacío = host actual).
     */
    private String domain = "";

    /**
     * Marca Secure en cookies.
     */
    private boolean secure = true;

    /**
     * Marca HttpOnly en cookies.
     */
    private boolean httpOnly = true;

    /**
     * SameSite: Lax/Strict/None.
     */
    private String sameSite = "None";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAccessTokenName() {
        return accessTokenName;
    }

    public void setAccessTokenName(String accessTokenName) {
        this.accessTokenName = accessTokenName;
    }

    public String getRefreshTokenName() {
        return refreshTokenName;
    }

    public void setRefreshTokenName(String refreshTokenName) {
        this.refreshTokenName = refreshTokenName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }
}
