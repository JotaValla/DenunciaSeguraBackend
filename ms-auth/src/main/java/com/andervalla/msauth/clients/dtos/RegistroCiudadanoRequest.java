package com.andervalla.msauth.clients.dtos;

public record RegistroCiudadanoRequest(
        String email,
        String nombre,
        String cedula
) {
}
