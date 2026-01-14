package com.andervalla.msusuarios.controllers.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarAliasRequest(
        @NotBlank
        @Size(min = 3, max = 40)
        String aliasPublico
) {
}
