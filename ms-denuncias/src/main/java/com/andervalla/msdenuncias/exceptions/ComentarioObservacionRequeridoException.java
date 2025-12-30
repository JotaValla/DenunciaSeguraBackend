package com.andervalla.msdenuncias.exceptions;

public class ComentarioObservacionRequeridoException extends RuntimeException {

    public ComentarioObservacionRequeridoException(Long denunciaId) {
        super("El comentario u observacion no puede ser nulo. Id=" + denunciaId);
    }
}
