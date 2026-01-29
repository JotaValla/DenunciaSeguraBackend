package com.andervalla.msauth.controllers.dtos.response;

/**
 * Respuesta del proceso de logout.
 */
public record LogoutResponse(
        String mensaje,
        boolean refreshTokenRevocado,
        boolean sesionInvalidada
) {
}
