package com.andervalla.msdenuncias.controllers.dtos.responses;

import java.util.List;

public record DenunciaEstadoHistorialResponse (
        Long denunciaId,
        List<EstadoCambio> items
){
}
