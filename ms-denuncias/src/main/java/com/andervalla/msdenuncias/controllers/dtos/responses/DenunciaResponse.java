package com.andervalla.msdenuncias.controllers.dtos.responses;

import com.andervalla.msdenuncias.clients.dtos.EvidenciaDTO;
import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.NivelAnonimatoEnum;

import java.time.Instant;
import java.util.List;

public record DenunciaResponse(
        Long id,
        String titulo,
        String descripcion,
        CategoriaDenunciaEnum categoriaDenuncia,
        EntidadResponsableEnum entidadResponsable,
        Double latitud,
        Double longitud,
        NivelAnonimatoEnum nivelAnonimato,
        EstadoDenunciaEnum estadoDenuncia,
        Long ciudadanoId,
        Long operadorId,
        String comentarioResolucion,
        String comentarioObservacion,
        List<EvidenciaDTO> evidenciaCreacionIds,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
