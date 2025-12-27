package com.andervalla.msdenuncias.exceptions;

import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;

public class DenunciaEstadoInvalidoException extends RuntimeException {

    public DenunciaEstadoInvalidoException(Long denunciaId,
                                           EstadoDenunciaEnum esperado,
                                           EstadoDenunciaEnum actual) {
        super("Estado invalido para denuncia. Id=" + denunciaId
                + ", esperado=" + esperado
                + ", actual=" + actual);
    }
}
