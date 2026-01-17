package com.andervalla.msauth.controllers.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordForgotRequest(
        @NotBlank
        @Size(min = 10, max = 10)
        String cedula
) {
}
