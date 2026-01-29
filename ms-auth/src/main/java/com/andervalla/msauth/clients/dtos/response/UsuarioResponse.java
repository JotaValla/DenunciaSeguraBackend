package com.andervalla.msauth.clients.dtos.response;

/**
 * Representa la respuesta de ms-usuarios al consultar o crear un usuario.
 */
public record UsuarioResponse(
        Long id,
        String cedula,
        String email,
        String nombre,
        String rol,
        String entidad,
        String aliasPublico,
        String publicCitizenId,
        String estado
) {
}
