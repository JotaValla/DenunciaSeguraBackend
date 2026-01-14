package com.andervalla.msauth.clients.dtos;

public record RegistroStaffRequest(
        String email,
        String nombre,
        String cedula,
        String rol,
        String entidad,
        String aliasPublico
) {
}
