package com.andervalla.msdenuncias.controllers.dtos.responses;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;

import java.time.Instant;

public record DenunciaListadoResponse(
        Long id,
        Long jefeId,
        Long operadorId,
        String titulo,
        Long ciudadanoId,
        Instant creadoEn,
        EntidadResponsableEnum entidadResponsable,
        EstadoDenunciaEnum estadoDenuncia,
        CategoriaDenunciaEnum categoriaDenuncia
) {
}
