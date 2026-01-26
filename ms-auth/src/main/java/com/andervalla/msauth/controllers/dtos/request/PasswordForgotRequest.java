package com.andervalla.msauth.controllers.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para solicitar el token de reseteo de contraseña.
 */
public record PasswordForgotRequest(
        @NotBlank
        @Size(min = 10, max = 10)
        String cedula
) {
}
