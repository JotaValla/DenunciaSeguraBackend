package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MarcarResolucionRequest(
        @NotBlank @NotNull String comentarioResolucion,
        List<String> evidenciasIds,
        Long resueltoPorId
) {
}
