package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record ValidarSolucionRequest (
        @NotNull Boolean aprobada,
        @NotNull String comentarioObservacion,
        Long validadoPorId
){}
