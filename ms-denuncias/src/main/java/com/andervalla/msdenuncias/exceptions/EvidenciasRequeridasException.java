package com.andervalla.msdenuncias.exceptions;

public class EvidenciasRequeridasException extends RuntimeException {

    public EvidenciasRequeridasException(Long denunciaId) {
        super("Debe proporcionar al menos una evidencia para resolver la denuncia. Id=" + denunciaId);
    }
}
