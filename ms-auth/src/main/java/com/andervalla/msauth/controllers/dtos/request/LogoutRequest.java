package com.andervalla.msauth.controllers.dtos.request;

/**
 * Payload para solicitar el cierre de sesión y la revocación del refresh token.
 */
public record LogoutRequest(
        String refreshToken
) {
}
