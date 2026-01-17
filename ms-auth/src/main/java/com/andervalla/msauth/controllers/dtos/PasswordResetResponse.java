package com.andervalla.msauth.controllers.dtos;

public record PasswordResetResponse(
        String mensaje,
        String token // dev only; en producción se enviaría por correo
) {
}
