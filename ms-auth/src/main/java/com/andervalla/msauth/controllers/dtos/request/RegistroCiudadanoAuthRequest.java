package com.andervalla.msauth.controllers.dtos.request;

import com.andervalla.msauth.validators.CedulaEcuatoriana;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de registro de un ciudadano desde el Authorization Server.
 */
public record RegistroCiudadanoAuthRequest(
        @NotBlank
        @Email
        @Size(max = 120)
        String email,
        @NotBlank
        @Size(max = 160)
        String nombre,
        @NotBlank
        @Size(min = 10, max = 10)
        @CedulaEcuatoriana
        String cedula,
        @NotBlank
        @Size(min = 8, max = 120)
        String password
) {
}
