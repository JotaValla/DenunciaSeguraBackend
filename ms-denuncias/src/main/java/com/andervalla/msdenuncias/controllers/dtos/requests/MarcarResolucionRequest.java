package com.andervalla.msdenuncias.controllers.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MarcarResolucionRequest(
        @NotBlank(message = "El comentario de resolución es obligatorio") @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres") String comentarioResolucion,
        @NotNull(message = "La lista de evidencias debe estar presente (aunque sea vacía)") @Size(max = 3, message = "No se permiten más de 3 evidencias") List<String> evidenciasIds
    ) {
    }

