package com.andervalla.msdenuncias.controllers.dtos.responses;

public record Reporter(
        Long id,          // solo para roles internos permitidos en REAL
        String alias,     // alias público o publicCitizenId en PSEUDOANONIMO
        boolean visible   // indica si se muestra algo al consumidor
) {
}
