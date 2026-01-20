package com.andervalla.msauth.controllers.dtos;

import com.andervalla.msauth.validators.CedulaEcuatoriana;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroStaffAuthRequest(
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
        @NotNull
        String rol,
        @NotNull
        String entidad,
        @Size(min = 8, max = 120)
        String password
) {
}
