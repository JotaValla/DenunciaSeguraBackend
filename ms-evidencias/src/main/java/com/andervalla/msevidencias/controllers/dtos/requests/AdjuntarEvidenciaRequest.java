package com.andervalla.msevidencias.controllers.dtos.requests;

import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdjuntarEvidenciaRequest(
        @NotNull EntidadTipoEnum entidadTipo,
        @NotNull Long entidadId,
        @NotNull(message = "La lista de evidencias debe estar presente (aunque sea vacía)") @Size(max = 3, message = "No se permiten más de 3 evidencias") List<String> evidenciasIds,
        Long usuarioId
        ) {
}
