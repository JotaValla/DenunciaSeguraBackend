package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ValidarSolucionRequest(
        @NotNull(message = "El veredicto es obligatorio") Boolean aprobada,

        @NotBlank(message = "El comentario no puede estar vacío") @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres") String comentarioObservacion,

        Long validadoPorId) {
}
