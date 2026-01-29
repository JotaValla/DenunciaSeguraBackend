package com.andervalla.msauth.clients.dtos.request;

/**
 * Payload enviado a ms-usuarios para crear un ciudadano.
 */
public record RegistroCiudadanoRequest(
        String email,
        String nombre,
        String cedula
) {
}
