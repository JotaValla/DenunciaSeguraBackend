package com.andervalla.msevidencias.controllers.dtos.responses;

import com.andervalla.msevidencias.models.Enums.EstadoEvidenciaEnum;

public record EvidenciaInternaResponse(
        String id,
        String url,
        String contentType,
        Long sizeBytes
) {
}
