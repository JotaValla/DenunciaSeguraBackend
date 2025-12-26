package com.andervalla.msdenuncias.controllers.dtos.responses;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.NivelAnonimatoEnum;

import java.time.Instant;
import java.util.List;

public record DenunciaResponse(
        Long id,
        String titulo,
        String descripcion,
        CategoriaDenunciaEnum categoriaDenuncia,
        Double latitud,
        Double longitud,
        NivelAnonimatoEnum nivelAnonimato,
        EstadoDenunciaEnum estadoDenuncia,
        Long ciudadanoId,
        Long operadorId,
        String comentarioResolucion,
        String comentarioObservacion,
        List<String> evidenciaCreacionIds,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
