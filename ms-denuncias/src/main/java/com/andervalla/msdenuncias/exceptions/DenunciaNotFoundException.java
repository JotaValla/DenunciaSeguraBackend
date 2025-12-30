package com.andervalla.msdenuncias.exceptions;

public class DenunciaNotFoundException extends RuntimeException {

    public DenunciaNotFoundException(Long denunciaId) {
        super("Denuncia no encontrada. Id=" + denunciaId);
    }
}
