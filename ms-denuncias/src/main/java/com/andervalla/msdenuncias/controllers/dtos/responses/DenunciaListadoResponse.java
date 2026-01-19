package com.andervalla.msdenuncias.controllers.dtos.responses;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;

import java.time.Instant;

public record DenunciaListadoResponse(
        Long id,
        String titulo,
        CategoriaDenunciaEnum categoriaDenuncia,
        //entidad, estado, fecha creacion
        EntidadResponsableEnum entidadResponsable,
        EstadoDenunciaEnum estadoDenuncia,
        Long ciudadanoId,
        Long operadorId,
        Long jefeId,
        Instant creadoEn
) {
}
