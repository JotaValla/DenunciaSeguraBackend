package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignarOperadorRequest(
        @NotNull(message = "El ID del operador es obligatorio") @Positive(message = "El ID del operador debe ser un número positivo") Long operadorId,

        @NotNull(message = "El ID del administrador que asigna es obligatorio") @Positive(message = "El ID del administrador debe ser un número positivo") Long asignadoPorId) {
}
