package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record AsignarOperadorRequest(
        @NotNull Long operadorId
) {
}
