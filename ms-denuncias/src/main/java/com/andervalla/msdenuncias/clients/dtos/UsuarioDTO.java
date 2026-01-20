package com.andervalla.msdenuncias.clients.dtos;

public record UsuarioDTO(
        Long id,
        String cedula,
        String email,
        String nombre,
        String rol,
        String entidad,
        String aliasPublico
) {
}
