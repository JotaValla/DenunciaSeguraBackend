package com.andervalla.msauth.controllers.dtos.response;

/**
 * Respuesta dev para exponer el token de reseteo generado.
 */
public record PasswordResetResponse(
        String mensaje,
        String token // dev only; en producción se enviaría por correo
) {
}
