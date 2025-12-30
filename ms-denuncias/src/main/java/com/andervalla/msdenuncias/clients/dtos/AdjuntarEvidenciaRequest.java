package com.andervalla.msdenuncias.clients.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record AdjuntarEvidenciaRequest(
        String entidadTipo,
        Long entidadId,
        List<String> evidenciasIds,
        Long usuarioId
) {
}
