package com.andervalla.msevidencias.controllers.dtos.requests;

import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdjuntarEvidenciaRequest(
        @NotNull EntidadTipoEnum entidadTipo,
        @NotNull Long entidadId,
        @NotEmpty List<String> evidenciasIds,
        Long usuarioId
        ) {
}
