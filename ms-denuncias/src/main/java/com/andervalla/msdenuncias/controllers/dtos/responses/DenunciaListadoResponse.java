package com.andervalla.msdenuncias.controllers.dtos.responses;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;

import java.time.Instant;

public record DenunciaListadoResponse(
        Long id,
        String titulo,
        Reporter ciudadano,
        Reporter operador,
        Reporter jefe,
        Instant creadoEn,
        EntidadResponsableEnum entidadResponsable,
        EstadoDenunciaEnum estadoDenuncia,
        CategoriaDenunciaEnum categoriaDenuncia
) {
}
