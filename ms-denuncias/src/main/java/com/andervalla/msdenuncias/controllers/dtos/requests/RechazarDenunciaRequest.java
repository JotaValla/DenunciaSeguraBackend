package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record RechazarDenunciaRequest(
        @NotBlank String motivo
) {
}
