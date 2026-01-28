package com.andervalla.msauth.services.auth;

/**
 * Resultado de la revocación de tokens y la invalidación de sesión.
 */
public record LogoutResult(
        boolean refreshTokenRevocado,
        boolean sesionInvalidada
) {
}
