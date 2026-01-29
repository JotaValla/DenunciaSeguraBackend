package com.andervalla.msdenuncias.controllers.dtos.responses;

import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;

import java.time.Instant;

public record EstadoCambio(
        EstadoDenunciaEnum estadoAnterior,
        EstadoDenunciaEnum estadoNuevo,
        String actorNombre,
        Instant ocurridoEn
) {
}
