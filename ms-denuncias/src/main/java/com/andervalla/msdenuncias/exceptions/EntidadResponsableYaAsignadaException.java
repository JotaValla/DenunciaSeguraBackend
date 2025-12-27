package com.andervalla.msdenuncias.exceptions;

public class EntidadResponsableYaAsignadaException extends RuntimeException {

    public EntidadResponsableYaAsignadaException(Long denunciaId) {
        super("La denuncia ya tiene entidad responsable asignada. Id=" + denunciaId);
    }
}
