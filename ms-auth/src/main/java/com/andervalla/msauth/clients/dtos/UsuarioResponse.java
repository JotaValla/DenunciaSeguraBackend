package com.andervalla.msauth.clients.dtos;

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
