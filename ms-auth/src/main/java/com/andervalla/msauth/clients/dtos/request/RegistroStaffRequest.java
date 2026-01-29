package com.andervalla.msauth.clients.dtos.request;

/**
 * Payload enviado a ms-usuarios para crear personal de staff.
 */
public record RegistroStaffRequest(
        String email,
        String nombre,
        String cedula,
        String rol,
        String entidad
) {
}
