package com.andervalla.msauth.controllers.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para confirmar el reseteo de contraseña con el token recibido.
 */
public record PasswordResetRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 120) String newPassword
) {
}
