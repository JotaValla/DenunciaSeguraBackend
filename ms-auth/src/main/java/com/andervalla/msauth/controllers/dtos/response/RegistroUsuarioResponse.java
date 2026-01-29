package com.andervalla.msauth.controllers.dtos.response;

/**
 * Respuesta genérica al crear usuarios desde ms-auth.
 */
public record RegistroUsuarioResponse(
        Long id,
        String publicCitizenId,
        String mensaje
) {
}
