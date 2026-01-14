package com.andervalla.msauth.controllers.dtos;

public record RegistroUsuarioResponse(
        Long id,
        String publicCitizenId,
        String mensaje
) {
}
