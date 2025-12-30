package com.andervalla.msdenuncias.exceptions;

public class EntidadResponsableNoAsignadaException extends RuntimeException {

    public EntidadResponsableNoAsignadaException(Long denunciaId) {
        super("La denuncia no tiene entidad responsable asignada. Id=" + denunciaId);
    }
}
